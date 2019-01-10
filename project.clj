(defproject applicosan "0.1.0-SNAPSHOT"
  :description "My personal Slack bot"
  :url "https://github.com/athos/applicosan"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :source-paths ["src"]
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [ataraxy "0.4.2"]
                 [cheshire "5.8.1"]
                 [clj-http "3.9.1"]
                 [com.novemberain/monger "3.5.0"]
                 [drains "0.1.0"]
                 [duct/core "0.7.0"]
                 [duct/module.logging "0.4.0"]
                 [duct/server.http.jetty "0.2.0" :exclusions [duct/logger]]
                 [integrant "0.7.0"]
                 [org.clojure/core.cache "0.7.2"]
                 [org.slf4j/slf4j-nop "1.7.25"]
                 [ring/ring-core "1.7.0" :exclusions [clj-time commons-fileupload]]
                 [ring/ring-json "0.4.0"]]
  :plugins [[duct/lein-duct "0.11.0"]]
  :middleware [lein-duct.plugin/middleware]
  :main ^:skip-aot applicosan.main
  :profiles {:repl {:repl-options {:init-ns user}}
             :dev {:source-paths ["env"]
                   :dependencies
                   [[org.clojure/tools.namespace "0.2.11"]
                    [integrant/repl "0.3.1"]
                    [ring/ring-mock "0.3.2"]]}
             :uberjar {:aot :all
                       :uberjar-name "applicosan-standalone.jar"}})
