(ns applicosan.rules.ack
  (:refer-clojure :exclude [test])
  (:require [applicosan.attachments.core :as attach]
            [applicosan.condition :as c]
            [applicosan.rules.core :as rules :refer [defrule]]
            [applicosan.rules.utils :as utils]
            [applicosan.slack :as slack]
            [duct.logger :as logger]
            [integrant.core :as ig]))

(defrule ping (c/message #"^ping") [event {:keys [logger] :as opts}]
  (logger/log logger :info ::ping)
  (utils/reply event opts "pong" :mention? true))

(defrule test (c/message #"^test") [event opts]
  (utils/reply-with-attachments event opts [attach/press-me]))

(defmethod ig/init-key :applicosan.rules/ack [_ opts]
  (rules/->rule-set opts [ping test]))
