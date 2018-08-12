(ns apprentice.core
  (:require [clojure.data.json :as json]
            [ataraxy.core :as ataraxy]
            [clojure.java.io :as io]
            [datomic.client.api :as d]
            [datomic.ion.lambda.api-gateway :as apigw]))

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

(defn hello [{:keys [headers body]}]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Hello, World!"})

(def routes
  {"/hello" [:hello]})

(def handler
  (ataraxy/handler
   {:routes routes
    :handlers {:hello hello}}))

(def app
  (apigw/ionize handler))
