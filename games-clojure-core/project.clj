(defproject gcc "1.0.0-SNAPSHOT"
  :min-lein-version "2.0.0"
  :java-source-paths ["java"]
  :dependencies [
                 [org.clojure/clojure "1.7.0"]
                 [org.clojure/data.json "0.2.6"]
                 [de.bertschneider/clj-geoip "0.2"]
                 [clj-time "0.10.0"]
                 ]

  :profiles {
             :precomp {:aot [gcc.enrichment gcc.game]}
             :dev {:plugins [[com.jakemccrary/lein-test-refresh "0.11.0"]]}
             }
  )
