(ns applicosan.slack
  (:require [applicosan.image :as image]
            [applicosan.attachments.core :as attach]
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

(defn post-message
  ([client channel contents]
   (post-message client channel contents {}))
  ([client channel {:keys [text attachments]} {:keys [url]}]
   (let [body (cond-> {:channel channel}
                text (assoc :text text)
                attachments (assoc :attachments (mapv attach/->map attachments)))]
     (http/post (or url (api-endpoint "/chat.postMessage"))
                {:headers (merge (basic-slack-headers client)
                                 {"Content-Type" "application/json; charset=utf-8"})
                 :body (cheshire/generate-string body)}))))

(defn post-image [client channel image]
  (http/post (api-endpoint "/files.upload")
             {:headers (basic-slack-headers client)
              :multipart [{:name "file" :content (image/->bytes image)}
                          {:name "filetype" :content "png"}
                          {:name "channels" :content channel}]}))

(defmethod ig/init-key :applicosan/slack [_ opts]
  (make-client (:bot-token opts) (:bot-id opts) (:bot-name opts)))
