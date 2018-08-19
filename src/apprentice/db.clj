(ns apprentice.db
  (:require [integrant.core :as ig]
            [monger.collection :as mc]
            [monger.core :as mg]))

(def ^:const COLL_WORKTIME "worktime")

(declare ensure-indexes)

(defmethod ig/init-key :app/db [_ {:keys [env]}]
  (let [db (mg/connect-via-uri (:mongodb-uri env))]
    (ensure-indexes db)
    db))

(defmethod ig/halt-key! :app/db [_ {:keys [conn]}]
  (mg/disconnect conn))

(defn ensure-indexes [{:keys [db]}]
  (mc/ensure-index db COLL_WORKTIME {:year -1 :month -1 :day 1} {:unique true}))
