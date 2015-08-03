(ns api.gamefiltering
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
(def all-games-uri
  "http://localhost:49421/games/range/?from=0&to=2436887217759")

(defn transform-fraglog [game]
  (clojure.walk/prewalk
    #(if (and (map? %1) (contains? %1 "_2")) (%1 "_2") %1) game))

(defonce mls (multi-lookup-service))

(def lookup-ip (memoize #(lookup mls %1)))

(defn attach-geo-info [game]
  (clojure.walk/prewalk
    #(if (and (map? %1) (contains? %1 "ip"))
      (let [modified-ip (clojure.string/replace (%1 "ip") "x" "1")
            ip-lookup (lookup-ip modified-ip)
            cn-map (when-let [cn (:country-name ip-lookup)] {"countryName" cn})
            cc-map (when-let [cc (:country-code ip-lookup)] {"countryCode" cc})
            ]
        (merge %1 cn-map cc-map))
      %1)
    game))

(defn remove-unnecessary-fields [game]
  (dissoc (dissoc game "simpleId") "startTime"))

(defn enrich-game [game]
  (-> game
      remove-unnecessary-fields
      attach-geo-info
      transform-fraglog))

(defn process-games [games]
  (->> games
       (sort-by #(% "startTime"))
       (map enrich-game)))

(defn ctf-players [ctf]
  (set (map #(% "name")
            (mapcat
              #(%1 "players")
              (vals (ctf "teams"))))))

(defn duel-players [duel]
  (set (keys (duel "players"))))

(defn players [game]
  (clojure.set/union
    (duel-players game)
    (ctf-players game)))

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

(def parse-time (memoize #(f/parse (f/formatters :date-time-no-ms) %1)))

(defn game-matches-type? [game type]
  (let [game-type (game "type")]
    (or
      (= type "games")
      (= type game-type)
      (and (= type "ctfs") (= "ctf" game-type))
      (and (= type "duels") (= "duel" game-type))
      )))

(defn game-matches-time? [game timecat timeval]
  (or
    (and (= timecat "from") (= timeval "start"))
    (and (= timecat "until") (= timeval "now"))
    (let [timevalreal (parse-time timeval)]
      (or
        (and (= timecat "from") (is-after-on? game timevalreal))
        (and (= timecat "until") (is-before-on? game timevalreal))
        ))

    (and (= timecat "at")
         (let [
               times-strings (clojure.string/split "a,b,c" #",")
               times (map parse-time times-strings)
               ]
           (some
             (fn [time] (t/equal? time (game-time game)))
             times))
         )

    ))

(defn game-matches-players? [game players]
  (or
    (empty? players)
    (nil? players)
    (has-players? game players)))

(defn game-matches? [game type timecat timeval player]
  (and
    (game-matches-type? game type)
    (game-matches-time? game timecat timeval)
    (game-matches-players? game player)
    ))
