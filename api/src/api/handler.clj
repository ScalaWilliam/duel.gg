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

(defn find-games [desc]

  (let
    [
     filtered-games (filter #(api.gamefiltering/game-matches? %1 desc) @recent-games)
     should-reverse (or (contains? desc :until) (contains? desc :to) (contains? desc :recent))
     games (if should-reverse (reverse filtered-games) filtered-games)
     ] (println "DESC= " desc) (take 25 games)
       ))

(defn typ [x] (if (= x "") "games" (subs x 1)))

(defn get-game-players [game]
  (cond (= (game "type") "duel")
        (keys (game "players"))
        (= (game "type") "ctf")
        (map #(% "name") (flatten (map #(% "players") (map second (game "teams")))))
        )
  )

(defn get-player-names []
  (sort (apply vector (set (flatten (map get-game-players @recent-games)))))
  )

(defroutes
  app-routes
  (GET "/" [] (str "Hello Worldss"))
  (GET "/everything/" [] (response @recent-games))
  (GET "/game/:id/" [id]
    (let [game (first (filter #(= (%1 "endTimeText") id) @recent-games))]
      (if
        (nil? game)
        (route/not-found "game not found")
        (response game)))
    )

  (GET "/player-names/" []
       (response (get-player-names))
    )
  (GET "/:type{ctf|duel}/games/recent/" {{type :type} :params {player "player"} :query-params}
    (response (find-games {:recent :recent :type type :players player})))
  (GET "/:type{ctf|duel}/games/until/:time/" {{type :type time :time} :params {player "player"} :query-params}
    (response (find-games {:until time :type type :players player})))
  (GET "/:type{ctf|duel}/games/to/:time/" {{type :type time :time} :params {player "player"} :query-params}
    (response (find-games {:to time :type type :players player})))
  (GET "/:type{ctf|duel}/games/after/:time/" {{type :type time :time} :params {player "player"} :query-params}
    (response (find-games {:after time :type type :players player})))
  (GET "/:type{ctf|duel}/games/from/:time/" {{type :type time :time} :params {player "player"} :query-params}
    (response (find-games {:from time :type type :players player})))
  (GET "/:type{ctf|duel}/games/first/" {{type :type} :params {player "player"} :query-params}
    (response (find-games {:first :first :type type :players player})))

  (GET "/games/recent/" {{player "player"} :query-params}
    (response (find-games {:recent :recent :players player})))
  (GET "/games/until/:time/" {{time :time} :params {player "player"} :query-params}
    (response (find-games {:until time :players player})))
  (GET "/games/to/:time/" {{time :time} :params {player "player"} :query-params}
    (response (find-games {:to time :players player})))
  (GET "/games/after/:time/" {{time :time} :params {player "player"} :query-params}
    (response (find-games {:after time :players player})))
  (GET "/games/from/:time/" {{time :time} :params {player "player"} :query-params}
    (response (find-games {:from time :players player})))
  (GET "/games/first/" {{player "player"} :query-params}
    (response (find-games {:first :first :players player})))


  (GET "/games/" {{game "game"} :query-params}
    (let [game-ids (set (if (string? game) [game] game))
          matching-games (filter
                           #(contains? game-ids (%1 "endTimeText"))
                           @recent-games)
          game-map (map #(hash-map (%1 "endTimeText") %1) matching-games)
          ]
      (response (apply merge game-map))
      )
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
