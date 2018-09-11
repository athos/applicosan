(ns applicosan.rules.worktime
  (:require [applicosan.models.worktime :as worktime]
            [applicosan.rules.core :as rules :refer [defrule]]
            [applicosan.rules.utils :as utils]
            [applicosan.slack :as slack]
            [applicosan.time :as time]
            [integrant.core :as ig]))

(defn post-worktime-chart [{:keys [channel]} {:keys [slack]} worktimes]
  (slack/post-image slack channel (worktime/generate-chart worktimes)))

(defn stringify-time [^long t]
  (if (> (Math/abs t) 60)
    (let [m (Math/abs (long (rem t 60)))]
      (str (long (quot t 60)) "時間"
           (if (= m 0) "ちょうど" (str m "分"))))
    (format "%d分" (long t))))

(defn aggregate-overtime [db time]
  (let [{:keys [year month]} (time/date-map time)
        worktimes (worktime/latest-worktimes db)]
    (-> (worktime/aggregate-overtime worktimes year month)
        (assoc :worktimes worktimes))))

(defn notify-overtime [event time {:keys [db] :as opts} & {:keys [excludes-today?]}]
  (let [{:keys [last total worktimes]} (aggregate-overtime db time)]
    (utils/reply event opts
           (cond->> (str "今月の残業時間は" (stringify-time total) "だよ")
             (not excludes-today?)
             (str "今日の残業時間は" (stringify-time last) "、")))
    (post-worktime-chart event opts worktimes)))

(defrule hello #"^hi|hello|おは|こんにちは" [event {:keys [db] :as opts}]
  (let [time (utils/event-time event)
        {:keys [total]} (aggregate-overtime db time)]
    (worktime/clock-in! db time)
    (utils/reply event opts "おはよー☀️" :mention? true)
    (utils/reply event opts (str "今月の残業時間は" (stringify-time total) "だよ"))))

(defrule clock-in #"(\d{1,2}):(\d{1,2})出社" [event {:keys [db] :as opts}]
  (let [[_ hours minutes] &match
        clockin-time (time/today (Long/parseLong hours) (Long/parseLong minutes))
        message (str "出社時間を" hours ":" minutes "で記録したよ！")]
    (worktime/clock-in! db clockin-time)
    (utils/reply event opts message :mention? true)))

(defrule bye #"^bye|goodbye|さようなら|ばいばい|おつかれ|お疲れ" [event {:keys [db] :as opts}]
  (let [time (utils/event-time event)]
    (worktime/clock-out! db time)
    (utils/reply event opts "おつかれさまー👋" :mention? true)
    (notify-overtime event time opts)))

(defrule clock-out #"(\d{1,2}):(\d{1,2})退社" [event {:keys [db] :as opts}]
  (let [[_ hours minutes] &match
        clockout-time (time/today (Long/parseLong hours) (Long/parseLong minutes))
        message (str "退社時間を" hours ":" minutes "で記録したよ！")]
    (worktime/clock-out! db clockout-time)
    (utils/reply event opts message :mention? true)
    (notify-overtime event clockout-time opts)))

(defrule check-overtime #"残業時間を?(?:確認|教えて)" [event opts]
  (notify-overtime event (utils/event-time event) opts :excludes-today? true))

(defmethod ig/init-key :applicosan.rules/worktime [_ opts]
  (rules/->rule-set opts [hello clock-in bye clock-out check-overtime]))
