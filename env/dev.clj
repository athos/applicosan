(ns dev
  (:require [clojure.java.io :as io]
            [clojure.pprint :refer :all]
            [clojure.repl :refer :all]
            [clojure.tools.namespace.repl :refer [refresh]]
            [duct.core :as duct]
            [integrant.repl :refer [clear go halt prep init reset reset-all]]
            [integrant.repl.state :refer [config system]]))

(defn read-config []
  (duct/read-config (io/resource "applicosan/config.edn")))

(clojure.tools.namespace.repl/set-refresh-dirs "src")

(duct/load-hierarchy)

(def profiles
  [:duct.profile/dev :duct.profile/local])

(integrant.repl/set-prep! #(duct/prep-config (read-config) profiles))
