(ns apprentice.core
  (:require [ataraxy.core :as ataraxy]
            [clojure.java.io :as io]
            [ring.middleware.json :refer [wrap-json-body wrap-json-params wrap-json-response]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.util.response :as res]))

(defn- anom-map
  [category msg]
  {:cognitect.anomalies/category (keyword "cognitect.anomalies" (name category))
   :cognitect.anomalies/message msg})

(defn- anomaly!
  ([name msg]
   (throw (ex-info msg (anom-map name msg))))
  ([name msg cause]
   (throw (ex-info msg (anom-map name msg) cause))))

(defn hello [{:keys [headers body params] :as req}]
  (res/response {:resp (str "Hello, " (or (:name params) "World") "!")}))

(def handler
  (ataraxy/handler
   {:routes {"/hello" ^:api [:hello]}
    :handlers {:hello hello}
    :middleware {:api #(-> %
                           (wrap-json-body {:keywords? true})
                           wrap-keyword-params
                           wrap-json-params
                           wrap-json-response)}}))
