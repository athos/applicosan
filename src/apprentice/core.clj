(ns apprentice.core
  (:gen-class)
  (:require [ataraxy.core :as ataraxy]
            [environ.core :refer [env]]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as jetty]
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

(def app
  (ataraxy/handler
   {:routes {"/hello" ^:api [:hello]}
    :handlers {:hello hello}
    :middleware {:api #(-> %
                           (wrap-json-body {:keywords? true})
                           wrap-keyword-params
                           wrap-json-params
                           wrap-json-response)}}))

(defn -main []
  (let [port (Long/parseLong (get env :port "8080"))]
    (jetty/run-jetty app {:port port})))
