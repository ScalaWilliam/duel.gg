(ns gg.duel
  (:require
    [clj-http.client :as client]
    [clojure.data.json :as json]
    )
  )

(defn is-duel? [game] (contains? game "players"))

(defn is-ctf? [game] (contains? game "teams"))

(defn games []
  (->
    "http://odin.duel.gg:49431/games/"
    (client/get)
    (:body)
    (json/read-str)
    ))

(defn players [duels]
  (->>
    duels
    (map #(keys (% "players")))
    flatten
    set))

(defn duels []
  (filter is-duel? (games)))

(defn players-who-beat
  [duels name]
  (->>
    duels
    (filter #(contains? (% "players") name))
    (filter #(not= name (% "winner")))
    (filter #(not= nil (% "winner")))
    (map #(% "winner"))
    set)
  )

