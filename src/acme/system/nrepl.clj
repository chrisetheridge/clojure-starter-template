(ns acme.system.nrepl
  (:require [clojure.java.io :as io]
            [integrant.core :as ig]
            [nrepl.middleware]
            [nrepl.server]))

(defonce *nrepl-server (atom nil))

(defn resolve-symbol [sym]
  (require (symbol (namespace sym)))
  (find-var sym))

(defn load-middleware
  "Loads middleware vars on the class path
  If the middleware var is a sequence of handlers then load
  all of those middlewares too.

  Returns a sequence of loaded middleware vars."
  [middlewares loaded-middleware]
  (loop [[sym & rest-syms :as load-seq] middlewares
         res                            loaded-middleware]
    (if (empty? (seq load-seq))
      res
      (recur rest-syms
             (try
               (let [var        (resolve-symbol sym)
                     middleware @var]
                 (if (sequential? middleware)
                   (load-middleware middleware res)
                   (conj res var)))
               (catch Exception e
                 (println "Error loading nrepl middleware" (assoc (ex-data e) :symbol sym))))))))

(defn make-middleware-stack
  "Adds in cider.nrepl implicitly (if on the class path), default nrepl middleware, and
  any other middleware configured using wrap-fn in nrepl system config.

  Returns a stack of middleware, sorted by middleware implementation order."
  [& middlewares]
  (-> (->> (cond-> (into [] middlewares)
             (io/resource "cider/nrepl.clj")
             (conj 'cider.nrepl/cider-middleware))
           (keep identity))
      (into ['nrepl.middleware/wrap-describe
             'nrepl.middleware.interruptible-eval/interruptible-eval
             'nrepl.middleware.load-file/wrap-load-file
             'nrepl.middleware.session/add-stdin
             'nrepl.middleware.session/session])
      (load-middleware [])
      nrepl.middleware/linearize-middleware-stack))

(defn nrepl-handler [middleware-stack]
  ((apply comp (reverse middleware-stack)) nrepl.server/unknown-op))


(defn start-nrepl-server!
  ([port]
   (start-nrepl-server! port {}))
  ([port {:keys [wrap-fn handler-fn]}]
   (let [server (nrepl.server/start-server :bind "0.0.0.0"
                                           :port port
                                           :handler (or handler-fn
                                                        (nrepl-handler (make-middleware-stack wrap-fn))))]
     (println "Started nREPL server on" port)
     (reset! *nrepl-server server)
     server)))

(def nrepl-port-file-name ".nrepl-port")

(defn start-local-nrepl-server!
  ([port]
   (start-local-nrepl-server! port {}))
  ([port config]
   (spit nrepl-port-file-name (str port))
   (start-nrepl-server! port config)))

(defmethod ig/init-key :acme.system/nrepl [_ {:keys [local? port] :as config}]
  (let [start-fn (if local?
                   start-local-nrepl-server!
                   start-nrepl-server!)]
    ;; Fallback to currently started server if the nrepl service is restarted
    {:server (if @*nrepl-server
               (spit nrepl-port-file-name (str port))
               (start-fn port config))
     :local? local?}))

(defmethod ig/halt-key! :acme.system/nrepl [_ {:keys [local?]}]
  ;; We only delete the .nrepl-port file instead of killing the server
  ;; This is to preserve repl connection whilst restarting systems
  (when local?
    (when (.exists (io/file nrepl-port-file-name))
      (io/delete-file nrepl-port-file-name)))
  nil)
