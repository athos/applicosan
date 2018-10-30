(ns applicosan.time
  (:import [java.time LocalDateTime ZonedDateTime ZoneId]
           [java.time.temporal ChronoUnit]
           [java.util Date]))

(defprotocol ToDate
  (->date [this]))

(extend-protocol ToDate
  Date
  (->date [this] this)

  LocalDateTime
  (->date [this]
    (->date (.atZone this (ZoneId/systemDefault))))

  ZonedDateTime
  (->date [this]
    (Date/from (.toInstant this))))

(defprotocol ToZonedDateTime
  (->zoned-datetime [this]))

(extend-protocol ToZonedDateTime
  Date
  (->zoned-datetime [this]
    (ZonedDateTime/ofInstant (.toInstant this) (ZoneId/systemDefault)))

  LocalDateTime
  (->zoned-datetime [this]
    (.atZone this (ZoneId/systemDefault)))

  ZonedDateTime
  (->zoned-datetime [this] this))

(defn datetime
  ([year month day hour minute]
   (datetime year month day hour minute 0))
  ([year month day hour minute second]
   (-> (LocalDateTime/of (int year) (int month) (int day)
                         (int hour) (int minute) (int second))
       ->zoned-datetime)))

(defn date-map [dt]
  (let [^ZonedDateTime zdt (->zoned-datetime dt)]
    {:year (.getYear zdt)
     :month (.getValue (.getMonth zdt))
     :day (.getDayOfMonth zdt)}))

(defn time-map [dt]
  (let [^ZonedDateTime zdt (->zoned-datetime dt)]
    {:hour (.getHour zdt)
     :minute (.getMinute zdt)}))

(defn ^ZonedDateTime now []
  (->zoned-datetime (LocalDateTime/now)))

(defn today
  ([] (today 0 0))
  ([^long hours, ^long minutes]
   (-> (now)
       (.truncatedTo ChronoUnit/DAYS)
       (.withHour hours)
       (.withMinute minutes))))
