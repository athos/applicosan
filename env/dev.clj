(ns dev
  (:require [applicosan.core]
            [clojure.java.io :as io]
            [clojure.pprint :refer :all]
            [clojure.repl :refer :all]
            [clojure.tools.namespace.repl :refer [refresh]]
            [duct.core :as duct]
            [integrant.repl :refer [clear go halt prep init reset reset-all]]
            [integrant.repl.state :refer [config system]]))

(defn read-config []
  (duct/read-config (io/resource "config.edn")))

(clojure.tools.namespace.repl/set-refresh-dirs "src")

(duct/load-hierarchy)

(integrant.repl/set-prep! (comp duct/prep read-config))
