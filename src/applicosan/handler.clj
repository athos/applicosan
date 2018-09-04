(ns applicosan.handler
  (:require [applicosan.rules.core :as rules]
            [applicosan.rules]
            [clojure.string :as str]
            [integrant.core :as ig]
            [ring.util.response :as res]))

(defmethod ig/init-key ::acme [_ {:keys [acme-challenge]}]
  (fn [{[_ challenge] :ataraxy/result}]
    (if acme-challenge
      (res/response (str challenge "." acme-challenge))
      (res/not-found "Not Found"))))

(defn- handle-mention [event-id event {:keys [slack cache] :as opts}]
  (when (and (not= (:user event) (:id slack))
             (not= (:username event) (:name slack))
             (not (contains? @cache event-id)))
    (swap! cache assoc event-id event)
    (let [message (str/replace (:text event) (str "<@" (:id slack) "> ") "")]
      (future (rules/apply-rule message event opts)))))

(defmethod ig/init-key ::slack [_ opts]
  (fn [{{:keys [type] :as params} :body-params}]
    (case type
      "url_verification" (res/response {:challenge (:challenge params)})
      "event_callback" (let [{:keys [type] :as event} (:event params)
                             opts (update opts :db :db)]
                         (case type
                           "app_mention" (handle-mention (:event_id params) event opts)
                           nil)
                         (res/response "ok"))
      (res/response "ok"))))
