(defproject frontend "0.1.0"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-json "0.3.1"]
                 [compojure "1.4.0"]
                 [clj-http "2.0.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/clojurescript "0.0-3308"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [cljs-http "0.1.35"]
                 [org.omcljs/om "0.9.0"]
                 [figwheel "0.3.7"]]

  :plugins [[lein-cljsbuild "1.0.6"]
            [lein-ring "0.9.6"]
            [lein-pdo "0.1.1"]
            [lein-figwheel "0.3.3"]]

  :aliases {"up" ["pdo" "cljsbuild" "auto" "dev," "ring" "server-headless"]}

  :min-lein-version "2.5.0"
  :uberjar-name "frontend-standalone.jar"

  :ring {:handler frontend.core/app
         :init frontend.core/init}

  :source-paths ["src/clj"]

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs"]
                        :compiler {:output-to "resources/public/js/app.js"
                                   :output-dir "resources/public/js/out"
                                   :optimizations :none
                                   :source-map true}}
                       {:id "release"
                        :source-paths ["src/cljs"]
                        :compiler {:output-to "resources/public/js/app.js"
                                   :optimizations :advanced
                                   :pretty-print false
                                   :preamble ["react/react.min.js"]
                                   :externs ["react/externs/react.js"]}}]})
