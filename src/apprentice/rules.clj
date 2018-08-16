(ns apprentice.rules
  (:require [apprentice.rules.core :as rules :refer [defrule]]
            [apprentice.slack :as slack]))

(defn reply [{:keys [channel]} {:keys [slack]} message]
  (slack/post-message slack channel message))

(defrule hello #"^(?:hi)|(?:hello)" [event opts]
  (reply event opts "Hi"))
