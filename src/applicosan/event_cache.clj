(ns applicosan.event-cache
  (:require [clojure.core.cache :as cache]
            [integrant.core :as ig]))

(defmethod ig/init-key :applicosan/event-cache [_ {:keys [ttl]}]
  (atom (cache/ttl-cache-factory {} :ttl ttl)))
