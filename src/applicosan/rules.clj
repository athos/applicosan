(ns applicosan.rules
  (:require [applicosan.models.worktime :as worktime]
            [applicosan.rules.core :as rules :refer [defrule]]
            [applicosan.slack :as slack]
            [applicosan.time :as time])
  (:import [java.util Date]))

(defn event-time [{:keys [event_ts]}]
  (let [[_ s ms] (re-matches #"^(\d+)\.(\d{3})\d+$" event_ts)]
    (Date. (+ (* 1000 (Long/parseLong s)) (Long/parseLong ms)))))

(defn reply [{:keys [channel]} {:keys [slack]} message]
  (slack/post-message slack channel message))

(defn stringify-time [^long t]
  (if (> (Math/abs t) 60)
    (let [m (Math/abs (long (rem t 60)))]
      (str (long (quot t 60)) "時間"
           (if (= m 0) "ちょうど" (str m "分"))))
    (format "%d分" (long t))))

(defn notify-overtime [event {:keys [db] :as opts} & {:keys [excludes-today?]}]
  (let [worktimes (worktime/latest-worktimes db)
        {:keys [last total]} (worktime/aggregate-overtime worktimes)]
    (reply event opts
           (cond->> (str "今月の残業時間は" (stringify-time total) "だよ")
             (not excludes-today?)
             (str "今日の残業時間は" (stringify-time last) "、")))))

(defrule hello #"^hi|hello|おは|こんにちは" [event {:keys [db] :as opts}]
  (worktime/clock-in! db (event-time event))
  (let [{:keys [total]} (worktime/aggregate-overtime db)]
    (reply event opts "おはよー☀️")
    (reply event opts (str "今月の残業時間は" (stringify-time total) "だよ"))))

(defrule clock-in #"(\d{1,2}):(\d{1,2})出社" [event {:keys [db] :as opts}]
  (let [[_ hours minutes] &match
        clockin-time (time/today (Long/parseLong hours) (Long/parseLong minutes))]
    (worktime/clock-in! db clockin-time)
    (reply event opts (str "出社時間を" hours ":" minutes "で記録したよ！"))))

(defrule bye #"^bye|goodbye|さようなら|ばいばい|おつかれ|お疲れ" [event {:keys [db] :as opts}]
  (worktime/clock-out! db (event-time event))
  (reply event opts "おつかれさまー👋")
  (notify-overtime event opts))

(defrule clock-out #"(\d{1,2}):(\d{1,2})退社" [event {:keys [db] :as opts}]
  (let [[_ hours minutes] &match
        clockout-time (time/today (Long/parseLong hours) (Long/parseLong minutes))]
    (worktime/clock-out! db clockout-time)
    (reply event opts (str "退社時間を" hours ":" minutes "で記録したよ！"))
    (notify-overtime event opts)))

(defrule check-overtime #"残業時間を?(?:確認|教えて)" [event opts]
  (notify-overtime event opts :excludes-today? true))
