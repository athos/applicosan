(ns applicosan.handler
  (:require [applicosan.event-cache :as cache]
            [applicosan.rules :as rules]
            [cheshire.core :as cheshire]
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
            (or (rules/apply-rule rules message event)
                (logger/log logger :warn ::no-rules-applied {:message message}))
            (catch Throwable t
              (logger/log logger :error ::error-on-mention t)))))
      (logger/log logger :info ::event-duplicated {:id event-id}))))

(defn- handle-event [event-id {:keys [type] :as event} {:keys [logger] :as opts}]
  (logger/log logger :info ::event-arrived {:type type :id event-id})
  (case type
    "app_mention" (handle-mention event-id event (update opts :db :db))
    nil)
  (res/response "ok"))

(defn- handle-interaction [{:keys [channel] :as params} {:keys [logger]}]
  (logger/log logger :info ::interaction-arrived
              {:id (:callback_id params) :actions (mapv :value (:actions params))})
  (res/response {:channel (:id channel) :text "Thank you for pressing me!"}))

(defn- extract-params [req]
  (or (:body-params req)
      (some-> (:params req)
              :payload
              (cheshire/parse-string keyword))))

(defmethod ig/init-key ::slack [_ {:keys [logger] :as opts}]
  (fn [req]
    (logger/log logger :debug ::request-arrived req)
    (let [{:keys [type] :as params} (extract-params req)]
      (case type
        "url_verification" (res/response {:challenge (:challenge params)})
        "event_callback" (handle-event (:event_id params) (:event params) opts)
        "interactive_message" (handle-interaction params opts)
        (res/response "ok")))))
