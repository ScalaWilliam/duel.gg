(ns myproject.handler
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.reload :refer [wrap-reload]]
            [clj-http.client :as client]
            [environ.core :refer [env]]))

(def home-page
  (html
    [:html
     [:head
      [:title "duel.gg alfa"]
      [:meta {:charset "utf-8"}]
      [:meta {:name    "viewport"
              :content "width=device-width, initial-scale=1"}]
      (include-css "//cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.2.0/css/bootstrap.css")
      (include-css "http://fonts.googleapis.com/css?family=Roboto:300,400,500,700,400italic")
      (include-css "http://fonts.googleapis.com/css?family=Roboto+Condensed:400,300")
      (include-css "assets/css/material-design-iconic-font.min.css")
      (include-css "https://cdnjs.cloudflare.com/ajax/libs/bootstrap-formhelpers/2.3.0/css/bootstrap-formhelpers.min.css")
      (include-css "assets/css/re-com.css")
      (include-css (if (env :dev) "css/site.css" "css/site.min.css"))
      ]
     [:body
      [:div#app
       [:h3 "ClojureScript has not been compiled!"]
       [:p "please run "
        [:b "lein figwheel"]
        " in order to start the compiler"]]
      (include-js "js/app.js")]]))

(defroutes routes
           (GET "/" [] home-page)
           (GET "/recent/" []
             (client/get "http://alfa.duel.gg/api/games/" {:accept :json})
             )
           (resources "/")
           (not-found "Not Found"))

(def app
  (let [handler (wrap-defaults #'routes site-defaults)]
    (if (env :dev) (-> handler wrap-exceptions wrap-reload) handler)))
