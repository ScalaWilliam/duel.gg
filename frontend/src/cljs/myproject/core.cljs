(ns myproject.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [ajax.core :refer [GET]])
  (:import goog.History))

(def state (atom {:x 2}))

;; -------------------------
;; Views

(defn set-duel! [duel]
  (swap! state assoc :duel duel))

(defn get-recent-games []
  (GET
    "/api/games/until=now/"
    {:response-format :json
     :handler         #(swap! state assoc :recent-games %)
     }
    ))

(defn render-mini-duel [duel]
  (let
    [players (duel "players")

     player-header (let [[a b] (keys players)]
                     (str a " vs " b))
     scores (map #((second %) "frags") (seq players))
     score-header (let [[a b] scores]
                    (str a "-" b))
     other-header (str (duel "mode") " @ " (duel "map") " • " (duel "startTimeText"))
     ]
    [:div.mini-duel
     [:h2.players player-header]
     [:h3.score score-header]
     [:h4.rest other-header]
     ])
  )

(defn render-ctf [ctf]

  [:div.ctf
   [:h2 (str (ctf "mode") " @ " (ctf "map"))]
   [:h3 (str "on " (ctf "startTimeText"))]
   [:div.teams
    (map (fn [[teamname,
               {players "players", flags "flags"}]]
           ^{:key teamname} [:div.team
                             [:h2.flags flags]
                             [:h3.team-name teamname]
                             [:div.team-players
                              [:ul
                               (map (fn [{name "name"}]
                                      ^{:key name} [:li name]
                                      ) players)
                               ]
                              ]]) (seq (ctf "teams")))
    ]
   ]
  )

(defn render-duel [duel]
  [:div.bigger-duel
   [:h2.mode-map
    (str (duel "mode") " @ " (duel "map"))
    ]
   [:h3.start-time (duel "startTimeText")]
   [:div.players
    (map (fn [[name player]]
           [:div.player
            [:h3.player-frags (player "frags")]
            [:h4.player-name name]
            ]
           ) (seq (duel "players")))
    ]
   ]
  )

(defn api-detail []
  [:section#api
   [:h2 "duel.gg alfa API"]
   [:p {:style {:font-size "small"}} [:a {:href "#/"} "go back to the home page"]]
   [:p "I'm providing a free game API to duel.gg users."]
   [:p "This is sufficient for all your use cases. Query as much as you like."]
   [:p "We return 25 results at a time - sorted by ascending time if 'from', descending if 'until'."]
   [:h3 "Example queries"]
   [:div (let [queries
               [
                "/duels/until=2015-08-01T18:54:35Z/"
                "/ctfs/from=2015-08-01T18:54:35Z/?player=w00p|foxie"
                "/duels/until=2015-08-01T18:54:35Z/?player=w00p|foxie"
                "/duels/from=start/?player=w00p|foxie"
                "/ctfs/until=now/"
                "/games/until=2015-08-01T18:54:35Z/?player=|RB|Honzik1&player=w00p|raffael"
                "/games/?game=2015-08-03T16:52:11Z&game=2015-08-03T15:42:41Z"
                "/game/2015-08-03T16:52:11Z/"
                ]]
               [:ul
                (for [query queries]

                  [:li [:a {:href (str "http://alfa.duel.gg/api" query)} query]]

                  )])]
   [:hr]
   [:p {:style {:font-size "small"}} [:a {:href "#/"} "go back to the home page"]]
   ]
  )

(defn api-page [] (do (api-detail)))

(defn home-page []
  (do
    [:div

     [:p {:style {:font-weight "bold"}} "New: " [:a {:href "#/api-info"} "duel.gg alfa API"]]

     (let [recent-games (@state :recent-games)]
       (for [game recent-games]
         (cond
           (= (game "type") "ctf") (render-ctf game)
           (= (game "type") "duel") (render-duel game)
           )
         )
       )

     (let [duel (@state :duel)]
       (if (nil? duel) [:p "No duel shown"]
                       [:div
                        ;[:div (render-duel duel)]
                        [:div (render-mini-duel duel)]
                        ]
                       )
       )
     (let [ctf (@state :ctf)
           ]
       (if (nil? ctf) [:p "No ctf available"]
                      [:div (render-ctf ctf)]
                      )
       )
     [:div [:a {:href "#/about"} "go to about page"]]]
    ))

(defn about-page []
  [:div [:h2 "About myproject"]
   [:div [:a {:href "#/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
                    (session/put! :current-page #'home-page))

(secretary/defroute "/api-info" []
                    (session/put! :current-page #'api-page))

(secretary/defroute "/about" []
                    (session/put! :current-page #'about-page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      EventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-root []
  (get-recent-games)
  ;(get-duel)
  ;(get-ctf)
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
