(ns apprentice.rules
  (:require [apprentice.models.worktime :as worktime]
            [apprentice.rules.core :as rules :refer [defrule]]
            [apprentice.slack :as slack]))

(defn reply [{:keys [channel]} {:keys [slack]} message]
  (slack/post-message slack channel message))

(defn stringify-time [t]
  (if (> (Math/abs t) 60)
    (format "%d時間%d分" (long (quot t 60)) (long (mod t 60)))
    (format "%d分" (long t))))

(defrule hello #"^hi|hello|おはよう|こんにちは" [event {:keys [db] :as opts}]
  (worktime/clock-in! db)
  (let [{:keys [total]} (worktime/aggregate-overtime db)]
    (reply event opts "おはよー☀️")
    (reply event opts (str "今月の残業時間は" (stringify-time total) "だよ"))))

(defrule bye #"^bye|goodbye|さようなら|ばいばい" [event {:keys [db] :as opts}]
  (worktime/clock-out! db)
  (let [{:keys [last total]} (worktime/aggregate-overtime db)]
    (reply event opts "おつかれさまー👋")
    (reply event opts (str "今日の残業時間は" (stringify-time last) "、"
                           "今月の残業時間は" (stringify-time total) "だよ"))))
