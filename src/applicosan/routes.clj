(ns applicosan.routes
  (:require [integrant.core :as ig]))

(defmethod ig/init-key :applicosan/routes [_ _]
  '{[:get "/.well-known/acme-challenge/" challenge] [:acme challenge]
    [:post "/slack"] ^:api [:slack]})
