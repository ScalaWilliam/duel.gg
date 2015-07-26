(ns frontend.duels
  (:require
    [clj-http.client :as client]
    [clojure.data.json :as json]
    )
  )

(defn games []
  (->
    ;"http://odin.duel.gg:49431/games/"
    "http://localhost:9000/games/"
    (client/get)
    (:body)
    (json/read-str)
    ))


(defn duels [] (get (games) "duels"))

