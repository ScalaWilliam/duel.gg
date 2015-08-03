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
    [clj-http.client :as client]
    ))


(defonce recent-games (atom []))

(defn get-games []
  (let [r (client/get api.gamefiltering/all-games-uri {:accept :json})
        games (json/read-str (:body r))]
    (api.gamefiltering/process-games games)))

(defn update-it []
  (println "UPDATE ME?")
  (future
    (reset! recent-games (get-games))))

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
  (GET "/games/" {{game "game"} :query-params}
    (let [game-ids (set (if (string? game) [game] game))
          matching-games (filter
                           #(contains? game-ids (%1 "startTimeText"))
                           @recent-games)
          game-map (map #(hash-map (%1 "startTimeText") %1) matching-games)
          ]
      (response (apply merge game-map))
      )
    )
  (GET "/game/:id/" [id]
    (let [game (first (filter #(= (%1 "startTimeText") id) @recent-games))]
      (if
        (nil? game)
        (route/not-found "game not found")
        (response game)))
    )
  (GET "/:type{games|ctfs|duels}/:timecat{from|until}=:tme{.*}/"
       {{type :type, timecat :timecat, time :tme} :params
        {player "player"}                         :query-params}
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

(defn wrap-cors
  "Allow requests from all origins"
  [handler]
  (fn [request]
    (let [response (handler request)]
      (update-in response
                 [:headers "Access-Control-Allow-Origin"]
                 (fn [_] "*")))))


(def app
  (-> (handler/site app-routes)
      wrap-json-response
      wrap-cors))
