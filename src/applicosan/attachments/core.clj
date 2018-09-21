(ns applicosan.attachments.core
  (:require [clojure.spec.alpha :as s]))

(s/def ::name keyword?)
(s/def ::text string?)
(s/def ::type #{:button})
(s/def ::value string?)
(s/def ::action (s/keys :req-un [::name ::text ::type]))

(defrecord Attachment [id fallback actions])

(defn make-attachment [id {:keys [fallback] :as opts} actions]
  (let [fallback (or fallback
                     "Your UI doesn't seem to support interactive message.")]
    (merge (->Attachment id fallback (vec actions))
           (dissoc opts :fallback))))

(s/fdef defattachment
  :args (s/cat :id symbol? :actions (s/* ::action)))

(defmacro defattachment [id & actions]
  `(def ~(with-meta id {})
     (make-attachment ~(str (ns-name *ns*) "/" (name id))
                      ~(meta id)
                      ~(vec actions))))

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
