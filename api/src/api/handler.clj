(ns api.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults api-defaults]]
            [org.httpkit.client :as http]
            [clojure.data.json :as json]
            [compojure.handler :as handler]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.util.response :refer [response]]
            ))

(defonce lol "HExH")

(defonce recent-games
         @(http/get "http://alfa.duel.gg/recent/"))

(def rg
  (json/read-str (:body recent-games)))

(defn ctf-players [ctf]
  (set (map
         #(% "name")
         (mapcat
           #(%1 "players")
           (vals (ctf "teams"))))))

(defn duel-players [duel]
  (set (keys (duel "players"))))

(defn players [game]
  (clojure.set/union (duel-players game) (ctf-players game)))

(defn has-player? [game name]
  (contains? (players game) name))

(defn game-time [game]
  (f/parse
    (f/formatters :date-time-no-ms)
    (game "startTimeText")))

(defn is-before-on? [game date]
  (or (t/before? (game-time game) date)
      (t/equal? date (game-time game))))

(defn is-after-on? [game date]
  (or (t/after? (game-time game) date)
      (t/equal? date (game-time game))))

(defn has-players? [game names]
  (let [game-players (players game)
        matching-players (flatten (vector (vector names)))]
    (not (empty? (clojure.set/intersection (set game-players) (set matching-players))))
    ))

(defn is-after-on-str? [game date-str]
  (is-after-on? game (f/parse (f/formatters :date-time-no-ms) date-str)))

(defn is-before-on-str? [game date-str]
  (is-before-on? game (f/parse (f/formatters :date-time-no-ms) date-str)))

(defn game-matches-params? [game params]
  (every?
    true?
    (remove
      nil?
      [
       (when (contains? params "from")
         (is-after-on? game (f/parse (f/formatters :date-time-no-ms) (params "from"))))

       (when (contains? params "until")
         (is-before-on? game (f/parse (f/formatters :date-time-no-ms) (params "until"))))

       (when (contains? params "player")
         (has-players? game (params "player")))

       (when (contains? params "type")
         (= (game "type") (params "type")))

       ])
    ))

(defroutes app-routes
           (GET "/" [] (str "Hello Worldss = " lol))
           (GET "/X/" [] (response {:foo "bar"}))
           (GET "/games/" {params :query-params}
             (response
               (filter #(game-matches-params? % params) rg)
               ))
           (GET "/recent/" []
             (str "LELel" (json/read-str (:body recent-games)))
             )
           (route/not-found "Not Found")
           )

;(def app
;  (wrap-defaults app-routes site-defaults))

;(def app
;  (wrap-json-response app-routes))

(def app
  (-> (handler/site app-routes)
      wrap-json-response))
