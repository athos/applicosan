(ns applicosan.rules.ack
  (:refer-clojure :exclude [test])
  (:require [applicosan.rules.core :as rules :refer [defrule]]
            [applicosan.rules.utils :as utils]
            [applicosan.slack :as slack]
            [duct.logger :as logger]
            [integrant.core :as ig]
            [applicosan.attachments.core :as attach]))

(defrule ping #"^ping" [event {:keys [logger] :as opts}]
  (logger/log logger :info ::ping)
  (utils/reply event opts "pong" :mention? true))

(defrule test #"^test" [event opts]
  (utils/reply-with-attachments event opts [attach/press-me]))

(defmethod ig/init-key :applicosan.rules/ack [_ opts]
  (rules/->rule-set opts [ping test]))
