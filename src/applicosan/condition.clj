(ns applicosan.condition
  (:refer-clojure :exclude [or])
  (:require [applicosan.attachments.core :as attach]
            [applicosan.event :as event]))

(defprotocol ICondition
  (-applicable? [this event])
  (-match [this event]))

(defn applicable? [condition event]
  (-applicable? condition event))

(defn match* [condition event]
  (-match condition event))

(defn match [condition event]
  (when (-applicable? condition event)
    (-match condition event)))

(defrecord MessageCondition [pattern]
  ICondition
  (-applicable? [_ event]
    (= (::event/type event) :message))
  (-match [_ event]
    (re-find pattern (::event/message event))))

(defn message [pattern]
  (->MessageCondition pattern))

(defrecord InteractionCondition [attachment action-name]
  ICondition
  (-applicable? [_ event]
    (= (::event/type event) :interaction))
  (-match [_ event]
    (when (= (:id attachment) (:callback_id event))
      (let [actions (:actions event)]
        (-> (if action-name
              (let [value (attach/value-of attachment action-name)]
                (filterv #(= (:value %) value) actions))
              actions)
            not-empty)))))

(defn interaction
  ([attachment]
   (interaction attachment nil))
  ([attachment action-name]
   (->InteractionCondition attachment action-name)))

(defrecord OrCondition [conditions]
  ICondition
  (-applicable? [_ event]
    (some #(-applicable? % event) conditions))
  (-match [_ event]
    (some #(and (-applicable? % event)
                (-match % event))
          conditions)))

(defn or [& conditions]
  (->OrCondition (vec conditions)))
