(defproject api "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [compojure "1.4.0"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [http-kit "2.1.19"]
                 [org.clojure/data.json "0.2.6"]
                 [clj-time "0.10.0"]
                 [ring/ring-json "0.4.0"]
                 ]
  :plugins [[lein-ring "0.9.6"]]
  :ring {:handler api.handler/app}
  )
