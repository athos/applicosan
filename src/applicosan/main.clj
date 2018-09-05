(ns applicosan.main
  (:gen-class)
  (:require [applicosan.core :as app]
            [clojure.java.io :as io]
            [duct.core :as duct]))

(duct/load-hierarchy)

(defn -main [& args]
  (let [keys (or (duct/parse-keys args) [:duct/daemon])]
    (-> (io/resource "applicosan/config.edn")
        duct/read-config
        (duct/prep keys)
        (duct/exec keys))))
