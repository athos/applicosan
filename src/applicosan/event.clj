(ns applicosan.event
  (:require [applicosan.event-cache :as cache]
            [clojure.string :as str]
            [integrant.core :as ig]))

(defrecord EventFactory [bot-id bot-name cache])

(defmulti ->event* (fn [factory {:keys [type]}] type))
(defmethod ->event* :default [_ _] nil)

(defn make-event [factory params]
  (->event* factory params))

(defmethod ->event* "event_callback" [factory {:keys [event] :as params}]
  (case (:type event)
    "app_mention"
    (when (and (not= (:user event) (:bot-id factory))
               (not= (:username event) (:bot-name factory)))
      (let [event-id (:id params)]
        (if (cache/cache-event! (:cache factory) event-id event)
          (let [message (str/replace (:text event) (str "<@" (:bot-id factory) "> ") "")]
            (assoc event ::type :message ::message message))
          (throw (ex-info "Event duplicated" {:cause ::event-duplicated :id event-id})))))
    nil))

(defmethod ->event* "interactive_message" [_ params]
  (-> params
      (assoc ::type :interaction)
      (update :channel :id)
      (update :user :id)))

(defmethod ig/init-key :applicosan.event/factory [_ {:keys [slack cache]}]
  (->EventFactory (:id slack) (:name slack) cache))
