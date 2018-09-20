(ns applicosan.handler
  (:require [applicosan.event :as event]
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

(defn- handle-event [params {:keys [factory rules logger] :as opts}]
  (try
    (if-let [{:keys [::event/type] :as event} (event/make-event factory params)]
      (do (logger/log logger :info ::event-arrived {:type type})
          (future
            (try
              (let [message (::event/message event)]
                (or (rules/apply-rule rules message event)
                    (logger/log logger :warn ::no-rules-applied {:message message})))
              (catch Throwable t
                (logger/log logger :error ::error-on-event-handling t)))))
      (logger/log logger :warn ::event-ignored params))
    (catch Exception e
      (let [ed (ex-data e)]
        (if-let [cause (:cause ed)]
          (logger/log logger :warn cause (dissoc ed :cause))
          (throw e)))))
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
        (handle-event params opts)))))
