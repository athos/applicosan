(ns applicosan.handler
  (:require [applicosan.event-cache :as cache]
            [applicosan.rules :as rules]
            [clojure.string :as str]
            [duct.logger :as logger]
            [integrant.core :as ig]
            [ring.util.response :as res]))

(defmethod ig/init-key ::acme [_ {:keys [acme-challenge]}]
  (fn [{[_ challenge] :ataraxy/result}]
    (if acme-challenge
      (res/response (str challenge "." acme-challenge))
      (res/not-found "Not Found"))))

(defn- handle-mention [event-id event {:keys [slack cache rules logger]}]
  (when (and (not= (:user event) (:id slack))
             (not= (:username event) (:name slack)))
    (if (cache/cache-event! cache event-id event)
      (let [message (str/replace (:text event) (str "<@" (:id slack) "> ") "")]
        (logger/log logger :debug ::handle-mention {:message message})
        (future
          (try
            (rules/apply-rule rules message event)
            (catch Throwable t
              (logger/log logger :error ::error-on-mention t)))))
      (logger/log logger :info ::event-duplicated {:id event-id}))))

(defn- handle-event [event-id {:keys [type] :as event} {:keys [logger] :as opts}]
  (logger/log logger :info ::event-arrived {:type type :id event-id})
  (case type
    "app_mention" (handle-mention event-id event (update opts :db :db))
    nil)
  (res/response "ok"))

(defmethod ig/init-key ::slack [_ opts]
  (fn [{{:keys [type] :as params} :body-params}]
    (case type
      "url_verification" (res/response {:challenge (:challenge params)})
      "event_callback" (handle-event (:event_id params) (:event params) opts)
      (res/response "ok"))))
