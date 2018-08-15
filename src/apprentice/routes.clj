(ns apprentice.routes
  (:require [integrant.core :as ig]))

(defmethod ig/init-key :app/routes [_ _]
  '{[:get "/.well-known/acme-challenge/" challenge] [:acme challenge]
    "/hello" ^:api [:hello]})
