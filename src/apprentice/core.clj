(ns apprentice.core
  (:require [ataraxy.core :as ataraxy]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [datomic.client.api :as d]
            [datomic.ion.lambda.api-gateway :as apigw]
            [ring.middleware.json :refer [wrap-json-body wrap-json-params wrap-json-response]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.util.response :as res]))

(def get-client
  "This function will return a local implementation of the client
interface when run on a Datomic compute node. If you want to call
locally, fill in the correct values in the map."
  (memoize #(d/client {:server-type :ion
                       :region "us-east-1"
                       :system "stu-8"
                       :query-group "stu-8"
                       :endpoint "http://entry.stu-8.us-east-1.datomic.net:8182/"
                       :proxy-port 8182})))

(defn- anom-map
  [category msg]
  {:cognitect.anomalies/category (keyword "cognitect.anomalies" (name category))
   :cognitect.anomalies/message msg})

(defn- anomaly!
  ([name msg]
   (throw (ex-info msg (anom-map name msg))))
  ([name msg cause]
   (throw (ex-info msg (anom-map name msg) cause))))

(defn ensure-dataset
  "Ensure that a database named db-name exists, running setup-fn
against a connection. Returns connection"
  [db-name setup-sym]
  (require (symbol (namespace setup-sym)))
  (let [setup-var (resolve setup-sym)
        client (get-client)]
    (when-not setup-var
      (anomaly! :not-found (str "Could not resolve " setup-sym)))
    (d/create-database client {:db-name db-name})
    (let [conn (d/connect client {:db-name db-name})
          db (d/db conn)]
      (setup-var conn)
      conn)))

(defn get-connection []
  (ensure-dataset "datomic-docs-tutorial"
                  'apprentice.examples.tutorial/load-dataset))

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

(def app
  (apigw/ionize handler))
