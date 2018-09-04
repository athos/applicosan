(ns applicosan.core
  (:require [ataraxy.core :as ataraxy]
            [clojure.core.cache :as cache]
            [integrant.core :as ig]
            [ring.middleware.json :refer [wrap-json-body wrap-json-params wrap-json-response]]))

(defmethod ig/init-key :applicosan/event-cache [_ {:keys [ttl]}]
  (atom (cache/ttl-cache-factory {} :ttl ttl)))

(defmethod ig/init-key :applicosan/handler [_ {:keys [routes controllers env]}]
  (ataraxy/handler
   {:routes routes
    :handlers controllers
    :middleware {:api #(-> %
                           (wrap-json-body {:keywords? true})
                           wrap-json-params
                           wrap-json-response)}}))
