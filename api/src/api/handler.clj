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
    [clj-time.core :as t]
    [clj-time.periodic :refer
     [periodic-seq]]))

;(def http-uri "http://localhost:49421/games/")
(def http-uri
  "http://localhost:49421/games/range/?from=0&to=2436887217759")

(defn get-games []
  @(http/get "http://localhost:49421/games/"))

(defonce recent-games
         (atom ["X"]))

(defn update-it []
  (http/get
    http-uri
    (fn [{:keys [status headers body error]}]
      (if error
        (println "Failed, exception is " error)
        (do
          (let [new-games (json/read-str body)]
              (reset! recent-games new-games)
            )
          ;(println "Got it" (pr-str (json/read-str body)))
          ;(swap! recent-games (get-games))
          ;(swap! recent-games (json/read-str body))
          )))
    )
  )

(defonce
  updater
  (chime-at
    (periodic-seq (t/now) (-> 15 t/seconds))
    (fn [_]
      (update-it)
      )))

(def lol "WHUTTTxxx")

(def rg @recent-games)

(defn ctf-players [ctf]
  (set
    (map
      #(% "name")
      (mapcat
        #(%1 "players")
        (vals (ctf "teams"))))))

(defn duel-players [duel]
  (set (keys (duel "players"))))

(defn players [game]
  (clojure.set/union
    (duel-players game)
    (ctf-players game)))

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
    (not
      (empty?
        (clojure.set/intersection
          (set game-players)
          (set matching-players))))))

(defn is-after-on-str? [game date-str]
  (is-after-on?
    game
    (f/parse
      (f/formatters :date-time-no-ms) date-str)))

(defn is-before-on-str? [game date-str]
  (is-before-on?
    game
    (f/parse
      (f/formatters :date-time-no-ms) date-str)))

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
       ])))

(defroutes
  app-routes
  (GET "/" [] (str "Hello Worldss = " lol))
  (GET "/W/" [] (response @recent-games))
  (GET "/X/" [] (response {:foo "bar"}))
  (GET "/games/" {params :query-params}
    (response
      (take 25
            (reverse (sort-by #(% "startTime") (filter #(game-matches-params? % params) @recent-games)))
            )
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
