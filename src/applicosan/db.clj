(ns applicosan.db
  (:require [integrant.core :as ig]
            [monger.collection :as mc]
            [monger.core :as mg]))

(def ^:const COLL_WORKTIME "worktime")

(declare ensure-indexes)

(defmethod ig/init-key :applicosan/db [_ {:keys [uri]}]
  (let [db (mg/connect-via-uri uri)]
    (ensure-indexes db)
    db))

(defmethod ig/halt-key! :applicosan/db [_ {:keys [conn]}]
  (mg/disconnect conn))

(defn ensure-indexes [{:keys [db]}]
  (mc/ensure-index db COLL_WORKTIME {:year -1 :month -1 :day -1} {:unique true}))

(defmethod ig/init-key :applicosan.db/mongodb [_ {:keys [db]}]
  (:db db))
