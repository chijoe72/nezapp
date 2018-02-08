(defproject nezapp "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [environ "1.1.0"]
                 [ring "1.6.3"]
                 [compojure "1.6.0"]
                 [com.taoensso/timbre "4.8.0"]
                 [clj-http "3.4.1"]
                 [clj-time "0.13.0"]
                 [org.clojure/data.json "0.2.6"]
                 [ring-json-params "0.1.3"]
                 [ring/ring-json "0.4.0"]
                 [cc.qbits/jet "0.7.11"]
                 [ring-cors "0.1.9"]
                 [matchbox "0.0.9"]
                 [digest "1.4.6"]
                 [org.clojars.akiel/digest "0.1"]]
  :main nezapp.core
  :target-path "target/%s"
  :uberjar-name "nezapp.jar"
  :profiles {:uberjar {:aot :all}})
