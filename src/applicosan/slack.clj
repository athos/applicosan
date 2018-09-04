(ns applicosan.slack
  (:require [applicosan.image :as image]
            [clj-http.client :as http]
            [cheshire.core :as cheshire]
            [integrant.core :as ig]))

(defrecord SlackClient [token id name])

(defn make-client [token id name]
  {:pre [(string? token) (string? id) (string? name)]}
  (map->SlackClient {:token token :id id :name name}))

(defn basic-slack-headers [client]
  {"Authorization" (str "Bearer " (:token client))})

(defn api-endpoint [path]
  (str "https://slack.com/api" path))

(defn post-message [client channel text]
  (http/post (api-endpoint "/chat.postMessage")
             {:headers (merge (basic-slack-headers client)
                              {"Content-Type" "application/json"})
              :body (cheshire/generate-string {:channel channel :text text})}))

(defn post-image [client channel image]
  (http/post (api-endpoint "/files.upload")
             {:headers (basic-slack-headers client)
              :multipart [{:name "file" :content (image/->bytes image)}
                          {:name "filetype" :content "png"}
                          {:name "channels" :content channel}]}))

(defmethod ig/init-key :applicosan/slack [_ opts]
  (make-client (:bot-token opts) (:bot-id opts) (:bot-name opts)))
