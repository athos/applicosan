(ns applicosan.rules.ack
  (:require [applicosan.rules.core :as rules :refer [defrule]]
            [applicosan.rules.utils :as utils]
            [applicosan.slack :as slack]
            [duct.logger :as logger]
            [integrant.core :as ig]))

(defrule ping #"^ping" [event {:keys [logger] :as opts}]
  (logger/log logger :info ::ping)
  (utils/reply event opts "pong" :mention? true))

(defmethod ig/init-key :applicosan.rules/ack [_ opts]
  (rules/->rule-set opts [ping]))
