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

(defn clans [game]
  (keep #(get % "clan") (tree-seq coll? seq game))
  )

(defn tags [game]
  (get game "tags"))

(defn team-clan [team]
  (let [player-clans (map #(get % "clan") (get team "players"))
        clans-set (set player-clans)]
    (cond
      (and (= 1 (count clans-set))) (first clans-set)))
  )

(defn game-type [game]
  (cond
    (contains? game "teams") "ctf"
    (contains? game "players") "duel"))

(defn is-ctf? [game] (= "ctf" (game-type game)))

(defn is-duel? [game] (= "duel" (game-type game)))

; - there are at least two players in the team
; - all players in the team belong to the same clan
(defn get-team-clan [team]
  (when-let [clan ((first (team "players")) "clan")]
    (if
      (and
        (> (count (team "players")) 1)
        (every? #(= (% "clan") clan) (team "players")))
      clan
      nil
      )))


(defn is-clanwar? [game]
  (when-let [evil (get-in game ["teams" "evil"])]
    (when-let [good (get-in game ["teams" "good"])]
      (when-let [evil-clan (get-team-clan evil)]
        (when-let [good-clan (get-team-clan good)]
          (not (= evil-clan good-clan))
          )
        )
      )
    ))


(gen-class :name gcc.game.GameReader
           :prefix "reader-"
           :methods [
                     [getPlayers [String] java.util.Collection]
                     [getClans [String] java.util.Collection]
                     [getUsers [String] java.util.Collection]
                     [getTags [String] java.util.Collection]
                     ])

(defn reader-getPlayers [_ game-string]
  (players (json/read-str game-string)))
(defn reader-getClans [_ game-string]
  (clans (json/read-str game-string)))
(defn reader-getTags [_ game-string]
  (tags (json/read-str game-string)))
(defn reader-getUsers [_ game-string]
  (users (json/read-str game-string)))