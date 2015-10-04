(ns gcc.enrichment
  (:require
    [clojure.data.json :as json]
    [clj-time.core :as t]
    [clj-time.coerce :as c]
    [clj-time.format :as f]
    [de.bertschneider.clj-geoip.core :refer :all]
    ))

(definterface ProvidesCountry
  (^String getCountryCode [^String ip])
  (^String getCountryName [^String ip]))

(definterface GetsPlayerInfo
  (^String getUsername [^String nickname ^org.joda.time.DateTime startTime ^String countryCode])
  (^String getClan [^String nickname ^org.joda.time.DateTime startTime ^String countryCode])
  )

(definterface GameEnricher
  (parseGame [gameJson])
  (withTransformedLog [game])
  (withGeoInfo [game ^gcc.enrichment.ProvidesCountry countryProvider])
  (withPlayerInfo [game ^gcc.enrichment.GetsPlayerInfo playerProvider])
  (withoutUnnecessaryFields [game])
  (getGameEndTime [game])
  (getGameStartTime [game])
  (withEndTime [game])
  (transformFraglog [game])
  (fullyEnriched [game ^gcc.enrichment.ProvidesCountry countryProvider ^gcc.enrichment.GetsPlayerInfo playerProvider])
  (toJson [game])
  (^java.util.Collection getPlayerNames [game])
  )

(gen-class
  :name gcc.enrichment.GameEnricherImpl
  :implements [gcc.enrichment.GameEnricher]
  :prefix "gei-"
  )

(defn gei-withPlayerInfo [_ game ^gcc.enrichment.GetsPlayerInfo pp]
  (clojure.walk/prewalk
    #(if (and (map? %1) (contains? %1 "ip"))

      (let
        [startTime (f/parse
                     (f/formatters :date-time-no-ms)
                     (game "startTimeText"))
         name (%1 "name")
         countryCode (%1 "countryCode")
         username-map (when-let [username (.getUsername pp name startTime countryCode)] {"user" username})
         clan-map (when-let [clan (.getClan pp name startTime countryCode)] {"clan" clan})
         ]
        (merge %1 username-map clan-map)
        )


      %1) game)
  )

(defn gei-toJson [_ game]
  (json/write-str game)
  )

(defn gei-parseGame [_ gameJson]
  (json/read-str gameJson))

(defn gei-withTransformedLog [_ game]
  (clojure.walk/prewalk
    #(if (and (map? %1) (contains? %1 "_2")) (%1 "_2") %1) game))

(defn gei-withGeoInfo [_ game pc]
  (clojure.walk/prewalk
    #(if (and (map? %1) (contains? %1 "ip"))
      (let [modified-ip (clojure.string/replace (%1 "ip") "x" "1")
            cn-map (when-let [cn (.getCountryName pc modified-ip)] {"countryName" cn})
            cc-map (when-let [cc (.getCountryCode pc modified-ip)] {"countryCode" cc})
            ]
        (merge %1 cn-map cc-map))
      %1)
    game))

(defn gei-withoutUnnecessaryFields [_ game]
  (dissoc game "simpleId" "startTime" "endTime"))

(defn gei-getGameEndTime [game]
  (f/parse
    (f/formatters :date-time-no-ms)
    (game "endTimeText")))

(defn gei-getGameStartTime [_ game]
  (f/parse
    (f/formatters :date-time-no-ms)
    (game "startTimeText")))

(defn gei-withEndTime [t game]
  (let [startTime (gei-getGameStartTime t game)
        endTime (t/plus startTime (t/minutes (int (game "duration"))))
        endTimeInt (c/to-long endTime)
        endTimeText (f/unparse (f/formatters :date-time-no-ms) endTime)
        ]
    (merge game {"endTime" endTimeInt "endTimeText" endTimeText})))

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

(defn gei-getPlayerNames [_ game] (players game))

(defn parse-time [time]
  (f/parse
    (f/formatters :date-time-no-ms)
    time))

(gen-class
  :name gcc.enrichment.ProvidesCountryImpl
  :implements [gcc.enrichment.ProvidesCountry]
  :prefix "pc-")

(defonce mls (multi-lookup-service))

(def lookup-ip (memoize #(lookup mls %1)))

(defn pc-getCountryCode [_ ip] (:country-code (lookup-ip ip)))
(defn pc-getCountryName [_ ip] (:country-name (lookup-ip ip)))



(defn gei-fullyEnriched [t game ^gcc.enrichment.ProvidesCountry countryProvider ^gcc.enrichment.GetsPlayerInfo playerProvider]
  (gei-withoutUnnecessaryFields
    t
    (gei-withPlayerInfo
      t
      (gei-withGeoInfo
        t
        (gei-withEndTime
          t
          (gei-withTransformedLog
            t game)
          ) countryProvider)
      playerProvider)
    )
  )
