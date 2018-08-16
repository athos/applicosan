(ns apprentice.controllers
  (:require [apprentice.slack :as slack]
            [integrant.core :as ig]
            [ring.util.response :as res]))

(defn acme [response]
  (fn [{[_ challenge] :ataraxy/result}]
    (if response
      (res/response (str challenge "." response))
      (res/not-found "Not Found"))))

(defn slack-event-handler [client]
  (fn [{{:keys [type] :as params} :params}]
    (case type
      "url_verification" (res/response {:challenge (:challenge params)})
      "event_callback" (let [{:keys [type] :as event} (:event params)]
                         (case type
                           "app_mention" (do (slack/post-message client (:channel event) (:text event))
                                             (res/response "ok"))
                           (res/response "ok")))
      (res/response "ok"))))

(defn hello [{:keys [headers body params] :as req}]
  (res/response {:resp (str "Hello, " (or (:name params) "World") "!")}))

(defmethod ig/init-key :app/controllers [_ {:keys [slack env]}]
  (let [acme-challenge (get env :acme-challenge)]
    {:acme (acme acme-challenge)
     :slack (slack-event-handler slack)
     :hello hello}))
