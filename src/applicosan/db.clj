(ns applicosan.db
  (:require [integrant.core :as ig]
            [mongo-driver-3.client :as mcl]
            [mongo-driver-3.collection :as mc]))

(def ^:const COLL_WORKTIME "worktime")

(defn- ensure-indexes [{:keys [db]}]
  (mc/create-index db COLL_WORKTIME {:year -1 :month -1 :day -1} {:unique? true}))

(defmethod ig/init-key :applicosan/db [_ {:keys [uri]}]
  (let [db (mcl/connect-to-db uri)]
    (ensure-indexes db)
    db))

(defmethod ig/halt-key! :applicosan/db [_ {:keys [client]}]
  (mcl/close client))

(defmethod ig/init-key :applicosan.db/mongodb [_ {:keys [db]}]
  (:db db))
