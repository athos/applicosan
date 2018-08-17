(ns apprentice.core
  (:gen-class)
  (:require [apprentice.controllers]
            [apprentice.routes]
            [apprentice.slack]
            [ataraxy.core :as ataraxy]
            [clojure.core.cache :as cache]
            [environ.core :refer [env]]
            [integrant.core :as ig]
            [monger.core :as mg]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.json :refer [wrap-json-body wrap-json-params wrap-json-response]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.util.response :as res]))

(defmethod ig/init-key :app/env [_ _]
  env)

(defmethod ig/init-key :app/db [_ {:keys [env]}]
  (mg/connect-via-uri (:mongodb-uri env)))

(defmethod ig/halt-key! :app/db [_ {:keys [conn]}]
  (mg/disconnect conn))

(defmethod ig/init-key :app/event-cache [_ _]
  (atom (cache/ttl-cache-factory {} :ttl 90000)))

(defmethod ig/init-key :app/handler [_ {:keys [routes controllers env]}]
  (ataraxy/handler
   {:routes routes
    :handlers controllers
    :middleware {:api #(-> %
                           (wrap-json-body {:keywords? true})
                           wrap-keyword-params
                           wrap-json-params
                           wrap-json-response)}}))

(defmethod ig/init-key :app/server [_ {:keys [handler env]}]
  (let [port (Long/parseLong (get env :port "8080"))]
    (jetty/run-jetty handler {:port port :join? false})))

(defmethod ig/halt-key! :app/server [_ server]
  (.stop server))

(def config
  {:app/env env
   :app/event-cache {}
   :app/slack {:env (ig/ref :app/env)}
   :app/db {:env (ig/ref :app/env)}
   :app/routes {}
   :app/controllers {:slack (ig/ref :app/slack)
                     :cache (ig/ref :app/event-cache)
                     :db (ig/ref :app/db)
                     :env (ig/ref :app/env)}
   :app/handler {:routes (ig/ref :app/routes)
                 :controllers (ig/ref :app/controllers)
                 :env (ig/ref :app/env)}
   :app/server {:handler (ig/ref :app/handler)
                :env (ig/ref :app/env)}})

(def system)

(defn init []
  (alter-var-root #'system (fn [_] (ig/init config))))

(defn -main []
  (init))
