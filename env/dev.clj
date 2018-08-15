(ns dev
  (:require [apprentice.core :as app]
            [clojure.tools.namespace.repl :refer [refresh]]
            [integrant.core :as ig]))

(defn go []
  (app/init)
  :started)

(defn stop []
  (when app/system
    (ig/halt! app/system)
    (alter-var-root #'app/system (constantly nil))
    :stopped))

(defn reset []
  (stop)
  (refresh :after 'dev/go))
