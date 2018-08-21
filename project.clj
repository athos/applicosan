(defproject applicosan "0.1.0-SNAPSHOT"
  :description "My personal Slack bot"
  :url "https://github.com/athos/applicosan"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :source-paths ["src"]
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [ataraxy "0.4.0"]
                 [clj-http "3.9.1"]
                 [com.novemberain/monger "3.1.0"]
                 [drains "0.1.0"]
                 [environ "1.1.0"]
                 [integrant "0.6.3"]
                 [org.clojure/core.cache "0.7.1"]
                 [ring/ring-core "1.7.0-RC1"]
                 [ring/ring-jetty-adapter "1.7.0-RC1"]
                 [ring/ring-json "0.4.0"]]
  :profiles {:dev {:source-paths ["env"]
                   :dependencies
                   [[org.clojure/tools.namespace "0.2.11"]
                    [ring/ring-mock "0.3.2"]]}
             :uberjar {:main applicosan.core
                       :aot :all
                       :uberjar-name "applicosan-standalone.jar"}})
