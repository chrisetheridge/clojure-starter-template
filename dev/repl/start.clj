(ns repl.start
  (:require [acme.system :as system]
            [clojure.edn :as edn]
            [clojure.tools.cli :as cli]))

(def cli-opts
  [[nil "--config DATA" "merges additional EDN data into the build config"
    :parse-fn edn/read-string
    :assoc-fn (fn [opts _k v]
                (when-not (map? v)
                  (throw (ex-info "--config-merge must yield a map value but didn't" {:v v})))
                (update opts :config-merge conj v))]])

(defn -main [& args]
  (let [options           (-> (cli/parse-opts args cli-opts)
                              :options)
        {:keys [systems]} (-> options
                              :config-merge
                              first)]
    (println "Starting local systems" systems)
    (->> (system/read-config)
         (system/prepare-and-start-systems! systems))
    (.addShutdownHook (Runtime/getRuntime)
                      (Thread. (fn []
                                 (println "Stopping systems")
                                 (system/stop-systems! systems))))))
