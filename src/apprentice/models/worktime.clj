(ns apprentice.models.worktime
  (:require [monger.collection :as mc]
            [monger.operators :as mo])
  (:import [java.util Date]
           [java.time LocalDateTime ZonedDateTime ZoneId]
           [java.time.temporal ChronoUnit]))

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
