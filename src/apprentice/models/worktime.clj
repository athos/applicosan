(ns apprentice.models.worktime
  (:require [drains.core :as d]
            [drains.utils :as dutils]
            [monger.collection :as mc]
            [monger.operators :as mo])
  (:import [java.time LocalDateTime ZonedDateTime ZoneId]
           [java.time.temporal ChronoUnit]
           [java.util Date]))

(defn ^ZonedDateTime now []
  (.atZone (LocalDateTime/now) (ZoneId/systemDefault)))

(defn ^ZonedDateTime start-of-day [^ZonedDateTime zdt]
  (.truncatedTo zdt ChronoUnit/DAYS))

(defn ->date [^ZonedDateTime zdt]
  (Date/from (.toInstant zdt)))

(defn current-datetime []
  (let [now (now)
        today (start-of-day now)]
    {:year (.getYear today)
     :month (.getValue (.getMonth today))
     :day (.getDayOfMonth today)
     :now (->date now)}))

(defn record-time! [db type]
  {:pre (#{:in :out} type)}
  (let [{:keys [now] :as time} (current-datetime)]
    (mc/update db "attendance"
               (select-keys time [:year :month :day])
               {mo/$set {type now}}
               {:upsert true})))

(def clock-in! #(record-time! % :in))
(def clock-out! #(record-time! % :out))

(defn- aggregate [db drain]
  (let [{:keys [year month]} (current-datetime)]
    (->> (mc/find-maps db "attendance" {:year year :month month})
         (d/reduce drain))))

(defn aggregate-overtime [db]
  (let [drain (d/with (map (fn [{:keys [in out]}]
                             (if out
                               (- (/ (- (.getTime out) (.getTime in)) 1000.0 60) (* 9 60))
                               0)))
                      (d/drains {:total (dutils/sum)
                                 :average (dutils/mean)}))]
    (aggregate db drain)))
