(ns frontend.core
  (:use ring.util.response)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [compojure.core :refer [GET POST defroutes]]
            [ring.middleware.json :as middleware]
            [frontend.duels :as duels]))

(defroutes app-routes
           (GET "/" [] (resource-response "index.html" {:root "public"}))
           (GET "/duels" [] (response (take 10 (duels/duels))))
           (GET "/widgets" []
             (response [{:name "Widget 1234"}
                        {:name "Widget 2"}
                        {:name "hihihihi"}
                        ]))
           (route/resources "/")
           (route/not-found "Page not found"))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)))
