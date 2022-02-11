(defproject applicosan "0.1.0-SNAPSHOT"
  :description "My personal Slack bot"
  :url "https://github.com/athos/applicosan"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :source-paths ["src"]
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [ataraxy "0.4.2"]
                 [cheshire "5.10.2"]
                 [clj-http "3.12.3"]
                 [drains "0.1.0"]
                 [duct/core "0.8.0"]
                 [duct/module.logging "0.5.0"]
                 [duct/server.http.jetty "0.2.1" :exclusions [duct/logger]]
                 [integrant "0.8.0"]
                 [mongo-driver-3 "0.5.0"]
                 [org.clojure/core.cache "1.0.225"]
                 [org.mongodb/mongodb-driver-sync "4.4.2"]
                 [org.slf4j/slf4j-nop "1.7.36"]
                 [ring/ring-core "1.9.5" :exclusions [clj-time commons-fileupload]]
                 [ring/ring-json "0.5.1"]]
  :plugins [[duct/lein-duct "0.11.0"]]
  :middleware [lein-duct.plugin/middleware]
  :main ^:skip-aot applicosan.main
  :profiles {:repl {:repl-options {:init-ns user}}
             :dev {:source-paths ["env"]
                   :dependencies
                   [[org.clojure/tools.namespace "1.2.0"]
                    [integrant/repl "0.3.2"]
                    [ring/ring-mock "0.4.0"]]}
             :uberjar {:aot :all
                       :uberjar-name "applicosan-standalone.jar"}})
