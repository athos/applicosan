(ns applicosan.slack
  (:require [clj-http.client :as http]
            [cheshire.core :as cheshire]
            [integrant.core :as ig]))

(defrecord SlackClient [token id name])

(defn make-client [token id name]
  {:pre [(string? token) (string? id) (string? name)]}
  (map->SlackClient {:token token :id id :name name}))

(defn basic-slack-headers [client]
  {"Authorization" (str "Bearer " (:token client))})

(defn post-message [client channel text]
  (http/post "https://slack.com/api/chat.postMessage"
             {:headers (merge (basic-slack-headers client)
                              {"Content-Type" "application/json"})
              :body (cheshire/generate-string {:channel channel :text text})}))

(defmethod ig/init-key :app/slack [_ {:keys [env]}]
  (make-client (:slack-bot-token env) (:slack-bot-id env) (:slack-bot-name env)))
