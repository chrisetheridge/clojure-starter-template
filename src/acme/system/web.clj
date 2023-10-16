(ns acme.system.web
    (:require [integrant.core :as ig]))

(defmethod ig/init-key :acme.system/web [_ _]
  (println "Starting web"))
