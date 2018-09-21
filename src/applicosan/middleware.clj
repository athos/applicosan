(ns applicosan.middleware
  (:require [integrant.core :as ig]
            [ring.middleware.json :refer [wrap-json-body wrap-json-params wrap-json-response]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]))

(defmethod ig/init-key ::api [_ _]
  (fn [handler]
    (-> handler
        wrap-keyword-params
        wrap-json-params
        wrap-json-response
        wrap-params)))
