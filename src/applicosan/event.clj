(ns applicosan.event
  (:require [clojure.core.cache :as cache]
            [clojure.string :as str]
            [integrant.core :as ig]))

(defrecord EventFactory [bot-id bot-name cache])

(defmulti ->event* (fn [factory {:keys [type]}] type))
(defmethod ->event* :default [_ _] nil)

(defn make-event [factory params]
  (->event* factory params))

(defn- cache-event! [cache event-id event]
  (let [[old _] (swap-vals! cache assoc event-id event)]
    (not (contains? old event-id))))

(defmethod ->event* "event_callback" [{:keys [cache] :as factory} {:keys [event] :as params}]
  (case (:type event)
    "app_mention"
    (when (and (not= (:user event) (:bot-id factory))
               (not= (:username event) (:bot-name factory)))
      (let [event-id (:event_id params)]
        (if (or (nil? cache) ;; cache disabled
                (cache-event! cache event-id event)) ;; event already cached
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
  (let [{:keys [ttl disabled?]} cache
        cache (when (not disabled?) (atom (cache/ttl-cache-factory {} :ttl ttl)))]
    (->EventFactory (:id slack) (:name slack) cache)))
