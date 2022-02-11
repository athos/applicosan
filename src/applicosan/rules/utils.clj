(ns applicosan.rules.utils
  (:require [applicosan.slack :as slack])
  (:import [java.util Date]))

(defn event-time [{:keys [ts]}]
  (let [[_ s ms] (re-matches #"^(\d+)\.(\d{3})\d+$" ts)]
    (Date. (+ (* 1000 (Long/parseLong s)) (Long/parseLong ms)))))

(defn reply [{:keys [user] :as event} {:keys [slack]} contents & {:keys [mention?]}]
  (let [{:keys [text] :as contents} (cond (string? contents) {:text contents}
                                          (vector? contents) {:attachments contents}
                                          :else contents)
        contents (assoc contents :text
                        (cond->> text
                          mention? (str "<@" user "> ")))
        opts (if-let [url (:response_url event)] {:url url} {})]
    (slack/post-message slack (:channel event) contents opts)))
