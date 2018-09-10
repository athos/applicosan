(ns applicosan.event-cache
  (:require [clojure.core.cache :as cache]
            [integrant.core :as ig]))

(defn cache-event!
  "Cache events in terms of event-id.

  Returns true if caching was successful. If disabled, caching will always succeed."
  [{:keys [cache disabled?]} event-id event]
  (or disabled?
      (let [[old _] (swap-vals! cache assoc event-id event)]
        (not (contains? old event-id)))))

(defmethod ig/init-key :applicosan/event-cache [_ {:keys [ttl disabled?]}]
  {:cache (atom (cache/ttl-cache-factory {} :ttl ttl))
   :disabled? (boolean disabled?)})
