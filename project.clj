(defproject apprentice "0.1.0-SNAPSHOT"
  :description "FIXME"
  :url "https://github.com/athos/apprentice"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[ataraxy "0.4.0"]
                 [org.clojure/clojure "1.9.0"]
                 [ring/ring-core "1.7.0-RC1"]
                 [ring/ring-jetty-adapter "1.7.0-RC1"]
                 [ring/ring-json "0.4.0"]]
  :jvm-opts ["--add-modules" "java.xml.bind"]
  :main apprentice.core
  :aot :all
  :uberjar-name "apprentice-standalone.jar"
  :profiles {:dev {:dependencies [[ring/ring-mock "0.3.2"]]}})
