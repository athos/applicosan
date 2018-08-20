(ns applicosan.rules
  (:require [applicosan.models.worktime :as worktime]
            [applicosan.rules.core :as rules :refer [defrule]]
            [applicosan.slack :as slack]
            [applicosan.time :as time]))

(defn reply [{:keys [channel]} {:keys [slack]} message]
  (slack/post-message slack channel message))

(defn stringify-time [^long t]
  (if (> (Math/abs t) 60)
    (format "%d時間%d分" (long (quot t 60)) (long (mod t 60)))
    (format "%d分" (long t))))

(defrule hello #"^hi|hello|おはよう|こんにちは" [event {:keys [db] :as opts}]
  (worktime/clock-in! db)
  (let [{:keys [total]} (worktime/aggregate-overtime db)]
    (reply event opts "おはよー☀️")
    (reply event opts (str "今月の残業時間は" (stringify-time total) "だよ"))))

(defrule clock-in #"(\d{1,2}):(\d{1,2})出社" [event {:keys [db] :as opts}]
  (let [[_ hours minutes] &match
        clockin-time (time/today (Long/parseLong hours) (Long/parseLong minutes))]
    (worktime/clock-in! db clockin-time)
    (reply event opts (str "出社時間を" hours ":" minutes "で記録したよ！"))))

(defrule bye #"^bye|goodbye|さようなら|ばいばい" [event {:keys [db] :as opts}]
  (worktime/clock-out! db)
  (let [{:keys [last total]} (worktime/aggregate-overtime db)]
    (reply event opts "おつかれさまー👋")
    (reply event opts (str "今日の残業時間は" (stringify-time last) "、"
                           "今月の残業時間は" (stringify-time total) "だよ"))))

(defrule clock-out #"(\d{1,2}):(\d{1,2})退社" [event {:keys [db] :as opts}]
  (let [[_ hours minutes] &match
        clockout-time (time/today (Long/parseLong hours) (Long/parseLong minutes))]
    (worktime/clock-out! db clockout-time)
    (reply event opts (str "退社時間を" hours ":" minutes "で記録したよ！"))))
