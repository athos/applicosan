(ns apprentice.controllers
  (:require [integrant.core :as ig]
            [ring.util.response :as res]))

(defn acme [response]
  (fn [{[_ challenge] :ataraxy/result}]
    (if response
      (res/response (str challenge "." response))
      (res/not-found "Not Found"))))

(defn slack [{{:keys [type] :as params} :params}]
  (case type
    "url_verification" (res/response {:challenge (:challenge params)})
    "event_callback" (let [{:keys [type] :as event} (:event params)]
                       (case type
                         "app_mention" (do (println "mentioned:" (:text event))
                                           (res/response "ok"))
                         (res/response "ok")))
    (res/response "ok")))

(defn hello [{:keys [headers body params] :as req}]
  (res/response {:resp (str "Hello, " (or (:name params) "World") "!")}))

(defmethod ig/init-key :app/controllers [_ {:keys [env]}]
  (let [acme-challenge (get env :acme-challenge)]
    {:acme (acme acme-challenge)
     :slack slack
     :hello hello}))
