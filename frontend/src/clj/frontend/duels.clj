(ns frontend.duels
  (:require
    [clj-http.client :as client]
    [clojure.data.json :as json]
    )
  )

(defn is-duel? [game] (contains? game "players"))

(defn games []
  (->
    "http://odin.duel.gg:49431/games/"
    (client/get)
    (:body)
    (json/read-str)
    ))

(defn duels []
  (filter is-duel? (games))
  )

