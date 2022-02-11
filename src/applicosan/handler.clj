(ns applicosan.handler
  (:require [applicosan.event :as event]
            [applicosan.rules :as rules]
            [ataraxy.core :as ataraxy]
            [cheshire.core :as cheshire]
            [duct.logger :as logger]
            [integrant.core :as ig]
            [ring.util.response :as res]))

(defmethod ig/init-key ::acme [_ {:keys [acme-challenge]}]
  (fn [{[_ challenge] :ataraxy/result}]
    (if acme-challenge
      (res/response (str challenge "." acme-challenge))
      (res/not-found "Not Found"))))

(defn- handle-event [params {:keys [factory rules logger]}]
  (try
    (if-let [{:keys [::event/type] :as event} (event/make-event factory params)]
      (do (logger/log logger :info ::event-arrived {:type type})
          (future
            (try
              (or (rules/apply-rule rules event)
                  (logger/log logger :warn ::no-rules-applied event))
              (catch Throwable t
                (logger/log logger :error ::error-on-event-handling t)))))
      (logger/log logger :warn ::event-ignored params))
    (catch Exception e
      (let [ed (ex-data e)]
        (if-let [cause (:cause ed)]
          (logger/log logger :warn cause (dissoc ed :cause))
          (throw e)))))
  (res/response "ok"))

(defn- extract-params [{:keys [params]}]
  (let [payload (:payload params)]
    (if (string? payload)
      (cheshire/parse-string payload keyword)
      params)))

(defmethod ig/init-key ::slack [_ {:keys [logger] :as opts}]
  (fn [req]
    (logger/log logger :debug ::request-arrived req)
    (let [{:keys [type] :as params} (extract-params req)]
      (case type
        "url_verification" (res/response {:challenge (:challenge params)})
        (handle-event params opts)))))

(defmethod ig/init-key :applicosan/handler [_ options]
  (ataraxy/handler options))
