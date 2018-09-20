(ns applicosan.condition
  (:require [applicosan.event :as event]))

(defprotocol ICondition
  (-match [this event]))

(defn match [condition event]
  (-match condition event))

(defrecord MessageCondition [pattern]
  ICondition
  (-match [this event]
    (re-find pattern (::event/message event))))

(defn message [pattern]
  (->MessageCondition pattern))
