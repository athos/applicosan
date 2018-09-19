(ns applicosan.rules.utils
  (:require [applicosan.models.worktime :as worktime]
            [applicosan.slack :as slack])
  (:import [java.util Date]))

(defn event-time [{:keys [event_ts]}]
  (let [[_ s ms] (re-matches #"^(\d+)\.(\d{3})\d+$" event_ts)]
    (Date. (+ (* 1000 (Long/parseLong s)) (Long/parseLong ms)))))

(defn reply [{:keys [channel user]} {:keys [slack]} message & {:keys [mention?]}]
  (let [message (cond->> message
                  mention? (str "<@" user "> "))]
    (slack/post-message slack channel message)))

(defn reply-with-attachments [{:keys [channel]} {:keys [slack]} attachments]
  (slack/post-attachments slack channel attachments))
