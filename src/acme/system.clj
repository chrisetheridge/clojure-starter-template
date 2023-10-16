(ns acme.system
  (:require [aero.core :as aero]
            [integrant.core :as ig]))

;;; System state

(defonce *system
  (atom nil))

(defn start-systems! [systems config]
  (reset! *system (ig/init config systems)))

(defn stop-systems! [systems]
  (doseq [k systems]
    (ig/halt-key! k @*system))
  (reset! *system nil))

(defn prepare-and-start-systems! [systems config]
  (ig/load-namespaces config)
  (start-systems! systems config))

;;; Config reading

(defmethod aero/reader 'ig/ref [_opts _tag value]
  (ig/ref value))

(def default-config-path
  "resources/config/main.edn")

(defn read-config
  ([]
   (read-config default-config-path))
  ([config-path]
   (aero/read-config config-path)))
