(ns apprentice.models.worktime
  (:require [apprentice.db :as db]
            [apprentice.time :as time]
            [drains.core :as d]
            [drains.utils :as dutils]
            [monger.collection :as mc]
            [monger.operators :as mo])
  (:import [java.util Date]))

(defn- record-time! [db type dt]
  {:pre (#{:in :out} type)}
  (mc/update db db/COLL_WORKTIME (time/date-map dt)
             {mo/$set {type (time/->date dt)}}
             {:upsert true}))

(defn clock-in!
  ([db] (clock-in! db (Date.)))
  ([db dt]
   (record-time! db :in dt)))

(defn clock-out!
  ([db] (clock-out! db (Date.)))
  ([db dt]
   (record-time! db :out dt)))

(defn- aggregate [db drain]
  (let [{:keys [year month]} (time/date-map (Date.))]
    (->> (mc/find-maps db db/COLL_WORKTIME {:year year :month month})
         (d/reduce drain))))

(defn aggregate-overtime [db]
  (let [drain (d/with (map (fn [{:keys [^Date in, ^Date out]}]
                             (if out
                               (- (/ (- (.getTime out) (.getTime in)) 1000.0 60) (* 9 60))
                               0)))
                      (d/drains {:total (dutils/sum)
                                 :average (dutils/mean)
                                 :last (d/drain (completing (fn [_ t] t)) 0)}))]
    (aggregate db drain)))
