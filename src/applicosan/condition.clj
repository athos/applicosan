(ns applicosan.condition
  (:refer-clojure :exclude [or])
  (:require [applicosan.attachments.core :as attach]
            [applicosan.event :as event]))

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

(defrecord InteractionCondition [attachment action-name]
  ICondition
  (-match [this event]
    (->> (:actions event)
         (sequence (comp (map #(attach/action-of attachment (:value %)))
                         (filter #(= (:name %) action-name))))
         first)))

(defn interaction [attachment action-name]
  (->InteractionCondition attachment action-name))

(defrecord OrCondition [conditions]
  ICondition
  (-match [this event]
    (reduce (fn [_ condition]
              (when-let [matched (-match condition event)]
                (reduced matched)))
            nil
            conditions)))

(defn or [& conditions]
  (->OrCondition (vec conditions)))
