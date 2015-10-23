(ns gcc.game
  (:require
    [clojure.data.json :as json]
    [clj-time.format :as f]
    [de.bertschneider.clj-geoip.core :refer :all]
    )
  )

(defn parse-game [gameJson]
  (json/read-str gameJson))

(defn game-to-json [game]
  (json/write-str game))

(defn start-time [game]
  (f/parse
    (f/formatters :date-time-no-ms)
    (game "startTimeText")))

(defn end-time [game]
  (f/parse
    (f/formatters :date-time-no-ms)
    (game "startTimeText")))

(defn end-time [game]
  (f/parse
    (f/formatters :date-time-no-ms)
    (game "endTimeText")))

(defn start-time [game]
  (f/parse
    (f/formatters :date-time-no-ms)
    (game "startTimeText")))

(defn duration [game] (int (game "duration")))

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

(defn users [game]
  (keep #(get % "user") (tree-seq coll? seq game))
  )

(defn team-clan [team]
  (let [player-clans (map #(get % "clan") (get team "players"))
        clans-set (set player-clans)]
    (cond
      (and (= 1 (count clans-set))) (first clans-set)))
  )

(defn game-type [game] (game "type"))

(defn is-ctf? [game] (= "ctf" (game-type game)))

(defn is-clanwar? [game]
  (map #(get % "teams"))
  (and (is-ctf? game) (every?  )
       ))


(defn is-duel? [game] (= "duel" (game-type game)))
