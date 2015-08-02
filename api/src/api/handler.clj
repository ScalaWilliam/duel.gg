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
    ))

;(def http-uri "http://localhost:49421/games/")
(def http-uri
  "http://localhost:49421/games/range/?from=0&to=2436887217759")

(defn get-games []
  @(http/get "http://localhost:49421/games/"))

(defonce recent-games
         (atom ["X"]))

(defn transform-fraglog [game]
  (clojure.walk/prewalk #(if (and (map? %1) (contains? %1 "_2")) (%1 "_2") %1) game))

(defonce mls (multi-lookup-service))

(defn attach-geo-info [game]
  (clojure.walk/prewalk
    #(if (and (map? %1) (contains? %1 "ip"))
      (let [modified-ip (clojure.string/replace (%1 "ip") "x" "1")
            ip-lookup (lookup mls modified-ip)
            cn-map (when-let [cn (:country-name ip-lookup)] {"countryName" cn})
            cc-map (when-let [cc (:country-code ip-lookup)] {"countryCode" cc})
            ]
        (merge %1 cn-map cc-map))
      %1)
    game))


(defn update-it []
  (http/get
    http-uri
    (fn [{:keys [status headers body error]}]
      (if error
        (println "Failed, exception is " error)
        (do
          (let [new-games (json/read-str body)]
            (reset! recent-games
                    (map #(dissoc (dissoc %1 "simpleId") "startTime")
                         (map attach-geo-info
                              (map transform-fraglog
                                   (sort-by #(% "startTime") new-games))))
                    )
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

(defn game-matches? [game type timecat timeval player]
  (let [timevalreal (f/parse (f/formatters :date-time-no-ms) timeval)]
    (and
      (or (= type "games") (= type (game "type")))
      (and
        (or
          (and (= timecat "from") (is-after-on? game timevalreal))
          (and (= timecat "until") (is-before-on? game timevalreal))
          )
        )
      (or
        (empty? player)
        (nil? player)
        (has-players? game player)
        )
      )
    )
  )

(defroutes
  app-routes
  (GET "/" [] (str "Hello Worldss = " lol))
  (GET "/W/" [] (response @recent-games))
  (GET "/X/" [] (response {:foo "bar"}))
  (GET "/:type{games|ctf|duel}/:timecat{from|until}=:tme{.*}/"

       {{type :type, timecat :timecat, time :tme} :params
        {player :player}                          :query-params}
    (let
      [games (filter #(game-matches? %1 type timecat time player) @recent-games)
       ] (response (take 25 games))
         )
    )
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
