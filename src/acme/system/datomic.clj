(ns acme.system.datomic
  (:require [datomic.api :as d]
            [integrant.core :as ig]))

(defonce *conn
  (atom nil))

(defn conn []
  @*conn)

(defn db []
  (d/db (conn)))

(defn set-connection! [uri]
  (reset! *conn (d/connect uri)))

(defmethod ig/init-key :acme.system/datomic [_ {:keys [uri]}]
  (d/create-database uri)
  (let [conn (set-connection! uri)]
    {:connection conn
     :uri        uri}))
