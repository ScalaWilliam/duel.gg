(ns myproject.server
  (:require [myproject.handler :refer [app]]
            [environ.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]]
            [org.httpkit.server :refer [run-server]]
            )
  (:gen-class))

 (defn -main [& args]
   (let [port (Integer/parseInt (or (env :port) "3000"))]
     (run-server app {:port port :join? false})))
