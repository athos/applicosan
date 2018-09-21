(ns applicosan.attachments.core
  (:require [clojure.spec.alpha :as s]))

(s/def ::name keyword?)
(s/def ::text string?)
(s/def ::type #{:button})
(s/def ::value string?)
(s/def ::action (s/keys :req-un [::name ::text ::type]))

(s/fdef defattachment
  :args (s/cat :id symbol? :actions (s/* ::action)))

(defrecord Attachment [id fallback actions])

(defmacro defattachment [id & actions]
  (let [{:keys [fallback] :as opts} (meta id)
        default-fallback  "Your UI doesn't seem to support interactive message."
        opts (assoc opts :fallback (or fallback default-fallback))]
    `(def ~(with-meta id {})
       (merge (map->Attachment {:id ~(str (ns-name *ns*) "/" (name id))
                                :actions [~@actions]})
              ~opts))))

(defn ->map [{:keys [id] :as attachment}]
  (into {:callback_id id} (dissoc attachment :id)))

(defn value-of [attachment action-name]
  (->> (:actions attachment)
       (filter #(= (:name %) action-name))
       first
       :value))

(defn action-of [attachment value]
  (first (filter #(= (:value %) value) (:actions attachment))))

(defn action-name-of [attachment value]
  (:name (action-of attachment value)))

(defattachment press-me
  {:name :press
   :text "Press me!"
   :type :button
   :value "pressed"})
