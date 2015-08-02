(ns api.handler
  (:require
    [compojure.core :refer :all]
    [compojure.route :as route]
    [ring.middleware.defaults :refer
     [wrap-defaults site-defaults api-defaults]]
    [org.httpkit.client :as http]
    [clojure.data.json :as json]
    [compojure.handler :as handler]
    [clj-time.core :as t]
    [clj-time.format :as f]
    [ring.middleware.json :refer
     [wrap-json-response]]
    [ring.util.response :refer
     [response]]
    [chime :refer [chime-at]]
    ;[clout.core :as clout]
    [clj-time.core :as t]
    [clj-time.periodic :refer
     [periodic-seq]
     ]
    [de.bertschneider.clj-geoip.core :refer :all]
    [api.gamefiltering]
    ))


(defonce recent-games (atom []))

(defn update-it []
  (http/get
    api.gamefiltering/all-games-uri
    (fn [{:keys [status headers body error]}]
      (if error
        (println "Failed, exception is " error)
        (reset! recent-games (api.gamefiltering/process-games (json/read-str body)))
    ))))

(defonce
  updater
  (chime-at
    (periodic-seq (t/now) (-> 15 t/seconds))
    (fn [_]
      (update-it)
      )))

(defroutes
  app-routes
  (GET "/" [] (str "Hello Worldss"))
  (GET "/everything/" [] (response @recent-games))
  (GET "/:type{games|ctf|duel}/:timecat{from|until}=:tme{.*}/"
       {{type :type, timecat :timecat, time :tme} :params
        {player "player"}                          :query-params}
    (let
      [
       filtered-games (filter #(api.gamefiltering/game-matches? %1 type timecat time player) @recent-games)
       sorted-games (if (= timecat "until") (reverse filtered-games) filtered-games)
       ]
      (response (take 25 sorted-games))
      )
    )
  (GET "/recent/" []
    (str "LELel" (json/read-str (:body recent-games)))
    )
  (route/not-found "Not Found")
  )

(def app
  (-> (handler/site app-routes)
      wrap-json-response))
