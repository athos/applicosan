(ns applicosan.attachments.core
  (:require [clojure.spec.alpha :as s]))

(s/def ::fallback string?)
(s/def ::opts (s/keys :opt-un [::fallback]))

(s/def ::name keyword?)
(s/def ::text string?)
(s/def ::type #{:button})
(s/def ::value string?)
(s/def ::action (s/keys :req-un [::name ::text ::type]))

(s/fdef defattachment
  :args (s/cat :id symbol? :opts ::opts :actions (s/+ ::action)))

(defmacro defattachment [id {:keys [fallback] :as opts} & actions]
  (let [default-fallback  "Your UI doesn't seem to support interactive message."
        opts (assoc opts :fallback (or fallback default-fallback))]
    `(def ~id
       (merge {:callback_id ~(str (ns-name *ns*) "/" (name id))
               :actions [~@actions]}
              ~opts))))

(defn action-of [attachment value]
  (first (filter #(= (:value %) value) (:actions attachment))))

(defn action-name-of [attachment value]
  (:name (action-of attachment value)))

(defattachment press-me {}
  {:name :press
   :text "Press me!"
   :type :button
   :value "pressed"})
