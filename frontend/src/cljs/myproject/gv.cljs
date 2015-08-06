(ns myproject.gv)

(defn is-duel? [game] (= (game "type") "duel"))

(defn is-ctf? [game] (= (game "type") "ctf"))
(defn render-player-flag [player]
  [:span {:style {"display" "inline-block"} :class (str "bfh-flag-" (player "countryCode"))}])
(defn render-ctf-team-player [player]
  [:div
   (render-player-flag player)
   (player "name")
   ]
  )
(defn render-ctf-team [name team]
  [:div.g-team [:h3 (str name " - " (team "flags"))]
   [:div.g-players
    (map render-ctf-team-player (team "players"))
    ]
   ]
  )

(defn render-ctf [ctf]
  [:div.g.g-ctf
   [:h3 (str (ctf "mode") " @ " (ctf "map") " at " (ctf "startTimeText"))]

    (let [
          goodScore (get-in ctf ["teams" "good" "flags"])
          evilScore (get-in ctf ["teams" "evil" "flags"])
          firstTeam (if (> goodScore evilScore) "good" "evil")
          secondTeam (if (= firstTeam "good") "evil" "good")
          ]

      [:div.g-teams
        (render-ctf-team firstTeam (get-in ctf ["teams" firstTeam]))
        (render-ctf-team secondTeam (get-in ctf ["teams" secondTeam]))
      ])
      ])

(defn render-duel-player [player]
  (let [name (first player)
        him (second player)]
      [:div.d-player
       [:h4 (render-player-flag him) (str name " - " (him "frags"))]
       ]
    )
  )

(defn render-duel [duel]
  [:div.d.d-duel
   [:h3 (str (duel "mode") " @ " (duel "map") " at " (duel "startTimeText"))]

   (let [
         players (reverse (sort-by #((second %) "frags") (seq (duel "players"))))
         ]
     [:div.d-players
      (render-duel-player (first players))
      (render-duel-player (second players))
      ]
     )
   ])

(defn render-game [game]
  (cond
    (is-duel? game) (render-duel game)
    (is-ctf? game) (render-ctf game)
    )
  )

(defn render-api-response [cc]
  [:div
   [:h2 "Games"]
   (let [games
         (cond
           (contains? cc "startTimeText") (vector cc)
           (map? cc) (map second (seq cc))
           :else cc
           )]
     (map render-game games)
     )
   ;[:div (pr-str cc)]]
   ]
  )