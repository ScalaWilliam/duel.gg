(defproject api "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :jvm-opts [
             "-XX:+UnlockCommercialFeatures"
             "-XX:+FlightRecorder"
             "-Dcom.sun.management.jmxremote"
             "-Dcom.sun.management.jmxremote.port=8888"
             "-Dcom.sun.management.jmxremote.authenticate=false"
             "-Dcom.sun.management.jmxremote.ssl=false"]
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [compojure "1.4.0"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [http-kit "2.1.19"]
                 [clj-http "2.0.0"]
                 [org.clojure/data.json "0.2.6"]
                 [environ "1.0.0"]
                 [clj-time "0.10.0"]
                 [ring/ring-json "0.4.0"]
                 [jarohen/chime "0.1.6"]
                 [ring/ring-mock "0.2.0"]
                 [de.bertschneider/clj-geoip "0.2"]
                 ]
  ;:plugins [[lein-ring "0.9.6"]]
  :ring {:handler api.handler/app}
  :profiles {:uberjar {:aot :all}}
  :main api.handler
  )
