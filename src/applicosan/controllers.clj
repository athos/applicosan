(ns applicosan.controllers
  (:require [applicosan.rules.core :as rules]
            [applicosan.rules]
            [clojure.string :as str]
            [integrant.core :as ig]
            [ring.util.response :as res]))

(defn acme [response]
  (fn [{[_ challenge] :ataraxy/result}]
    (if response
      (res/response (str challenge "." response))
      (res/not-found "Not Found"))))

(defn handle-mention [event {:keys [slack cache] :as opts}]
  (when (and (not= (:user event) (:id slack))
             (not= (:username event) (:name slack))
             (not (contains? @cache (:event_id event))))
    (swap! cache assoc (:event_id event) event)
    (let [message (str/replace (:text event) (str "<@" (:id slack) "> ") "")]
      (future (rules/apply-rule message event opts)))))

(defn slack-event-handler [opts]
  (fn [{{:keys [type] :as params} :params}]
    (case type
      "url_verification" (res/response {:challenge (:challenge params)})
      "event_callback" (let [{:keys [type] :as event} (:event params)]
                         (case type
                           "app_mention" (handle-mention event opts)
                           nil)
                         (res/response "ok"))
      (res/response "ok"))))

(defmethod ig/init-key :app/controllers [_ {:keys [env] :as opts}]
  (let [acme-challenge (get env :acme-challenge)]
    {:acme (acme acme-challenge)
     :slack (slack-event-handler (update opts :db :db))}))
