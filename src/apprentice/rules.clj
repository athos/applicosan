(ns apprentice.rules
  (:require [apprentice.rules.core :as rules :refer [defrule]]
            [apprentice.slack :as slack]
            [apprentice.models.worktime :as worktime]))

(defn reply [{:keys [channel]} {:keys [slack]} message]
  (slack/post-message slack channel message))

(defrule hello #"^hi|hello|おはよう|こんにちは" [event {:keys [db] :as opts}]
  (worktime/clock-in! db)
  (reply event opts "おはよー☀️"))

(defrule bye #"^bye|goodbye|さようなら|ばいばい" [event {:keys [db] :as opts}]
  (worktime/clock-out! db)
  (reply event opts "おつかれさまー👋"))
