(ns gcc.enrichment
  (:require
    [clj-time.core :as t]
    [clj-time.coerce :as c]
    [clj-time.format :as f]
    [de.bertschneider.clj-geoip.core :refer :all]
    [gcc.game :refer :all]
    [clojure.data.json :as json]))

(defonce mls (multi-lookup-service))

(def lookup-ip (memoize #(lookup mls %1)))

(defn player-with-geo-info [player]
  (let [modified-ip (clojure.string/replace (player "ip") "x" "1")
        cn-map (when-let [cn (:country-name (lookup-ip modified-ip))] {"countryName" cn})
        cc-map (when-let [cc (:country-code (lookup-ip modified-ip))] {"countryCode" cc})
        ]
    (merge player cn-map cc-map))
  )

(defn walk-players [game fn]
  (clojure.walk/prewalk
    #(if (and (map? %1) (contains? %1 "ip"))
      (let [r (fn %1)] (if (nil? r) %1 r)
                       )
      %1)
    game))

(defn walk-game-players [game fn]
  (clojure.walk/prewalk
    #(if (and (map? %1) (contains? %1 "ip"))
      (let [r (fn game %1)] (if (nil? r) %1 r))
      %1)
    game)
  )

(defn with-geo-info [game]
  (walk-players game player-with-geo-info))

(defn walk-teams [game fn]
  (clojure.walk/prewalk
    #(if (and (map? %1) (contains? %1 "players") (contains? %1 "name")) (fn %1) %1)
    game)
  )

(defn transform-score-log [game]
  (clojure.walk/prewalk
    #(if (and (map? %1) (contains? %1 "_2")) (%1 "_2") %1) game))

(defn without-redundant-fields [game]
  (dissoc game "simpleId" "startTime" "endTime"))

(defn with-end-time [game]
  (let [endTime (t/plus (start-time game) (t/minutes (duration game)))
        endTimeInt (c/to-long endTime)
        endTimeText (f/unparse (f/formatters :date-time-no-ms) endTime)
        ]
    (merge game {"endTime" endTimeInt "endTimeText" endTimeText})))

(definterface PlayerLookup
  (^String lookupUserId [^String nickname ^org.joda.time.DateTime atTime])
  (^String lookupClanId [^String nickname ^org.joda.time.DateTime atTime])
  )

(defn with-player-user [player-lookup game player]
  (when-let [player-user-id (.lookupUserId player-lookup (player "name") (start-time game))] (merge player {"user" player-user-id})))

(defn with-player-clan [player-lookup game player]
  (when-let [player-clan-id (.lookupClanId player-lookup (player "name") (start-time game))] (merge player {"clan" player-clan-id})))

(defn attach-team-clan [team]
  (if-let [clan (team-clan team)] (merge team {"clan" clan}) team))

(defn with-team-clans [game]
  (walk-teams game attach-team-clan))

(defn with-player-users [game player-lookup]
  (walk-game-players game (partial with-player-user player-lookup)))

(defn with-player-clans [game player-lookup]
  (walk-game-players game (partial with-player-clan player-lookup)))

(defn with-game-clan-info [game]
  (let [clans (remove nil? (map (comp #(get % "clan") second) (game "teams")))
        exactly-two-clans (and (seq? clans) (= (count clans) 2))
        ] (if exactly-two-clans
            (merge game {"clanwar" clans})
            game)
          ))

(defn with-type [game]
  (merge game (when-let [type (game-type game)] {"type" type})))

(defn with-tags [game]
  (merge
    game
    {"tags"
     (remove nil? [
      (game-type game)
      (if (is-clanwar? game) "clanwar" nil)
      ])}))

(defn enrich-game [game player-lookup]
  (->
    game
    with-type
    transform-score-log
    without-redundant-fields
    with-end-time
    with-geo-info
    (with-player-users player-lookup)
    (with-player-clans player-lookup)
    with-team-clans
    with-game-clan-info
    with-tags
    )
  )


(gen-class :name gcc.enrichment.Enricher
           :init init
           :prefix "enricher-"
           :state state
           :constructors {[gcc.enrichment.PlayerLookup] []}
           :methods [
                     [enrichJsonGame [String] String]
                     [enrichJsonGames [String] String]
                     ])

(defn enricher-init [player-lookup] [[] player-lookup])

(defn enricher-enrichJsonGames [this json-games]
  (let
    [json-games-map (json/read-str json-games)
     ;_ (println "KKK" (pr-str json-games-map))
     mapped-games (into {} (for [[k v] json-games-map] [k (enrich-game v (.state this))]))
     ]
    (json/write-str mapped-games)
    )
  )

(defn enricher-enrichJsonGame [this json-game]
  (->
    json-game
    parse-game
    (enrich-game (.state this))
    game-to-json
    ))
