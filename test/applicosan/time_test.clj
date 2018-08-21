(ns applicosan.time-test
  (:require [applicosan.time :as time]
            [clojure.test :as t :refer [deftest is testing]])
  (:import [java.time LocalDateTime ZonedDateTime ZoneId]
           [java.util Date TimeZone]))

(t/use-fixtures :once
  (fn [f]
    (let [tz (TimeZone/getDefault)]
      (TimeZone/setDefault (TimeZone/getTimeZone "Asia/Tokyo"))
      (try
        (f)
        (finally
          (TimeZone/setDefault tz))))))

(deftest ->date-test
  (is (= #inst "2018-08-21T09:00:00+09:00"
         (time/->date (LocalDateTime/of 2018 8 21 9 0))
         (time/->date (.atZone (LocalDateTime/of 2018 8 21 9 0) (ZoneId/systemDefault))))))

(deftest ->zoned-datetime-test
  (is (= (.atZone (LocalDateTime/of 2018 8 21 9 0) (ZoneId/systemDefault))
         (time/->zoned-datetime #inst "2018-08-21T09:00:00+09:00")
         (time/->zoned-datetime (LocalDateTime/of 2018 8 21 9 0)))))

(deftest date-map-test
  (is (= {:year 2018 :month 8 :day 21}
         (time/date-map (LocalDateTime/of 2018 8 21 9 0)))))

(deftest today-test
  (with-redefs [time/now (fn [] (time/->zoned-datetime (LocalDateTime/of 2018 8 21 9 0)))]
    (is (= (time/->zoned-datetime (LocalDateTime/of 2018 8 21 0 0))
           (time/today)))
    (is (= (time/->zoned-datetime (LocalDateTime/of 2018 8 21 12 35))
           (time/today 12 35)))))
