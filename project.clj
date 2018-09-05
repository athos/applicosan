(defproject applicosan "0.1.0-SNAPSHOT"
  :description "My personal Slack bot"
  :url "https://github.com/athos/applicosan"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :source-paths ["src"]
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [clj-http "3.9.1"]
                 [com.novemberain/monger "3.1.0"]
                 [drains "0.1.0"]
                 [duct/core "0.6.2"]
                 [duct/module.ataraxy "0.2.0"]
                 [duct/module.logging "0.3.1"]
                 [duct/module.web "0.6.4"]
                 [integrant "0.6.3"]
                 [org.clojure/core.cache "0.7.1"]
                 [ring/ring-json "0.4.0"]]
  :plugins [[duct/lein-duct "0.10.6"]
            [lein-with-env-vars "0.2.0-SNAPSHOT"]]
  :env-vars [".env-vars"]
  :main ^:skip-aot applicosan.main
  :profiles {:repl {:repl-options {:init-ns user}}
             :dev {:source-paths ["env"]
                   :dependencies
                   [[org.clojure/tools.namespace "0.2.11"]
                    [integrant/repl "0.3.1"]
                    [ring/ring-mock "0.3.2"]]}
             :uberjar {:aot :all
                       :uberjar-name "applicosan-standalone.jar"}})
