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
    "/recent/"
    {:response-format :json
     :handler         #(swap! state assoc :recent-games %)
     }
    ))

(defn get-ctf []
  (GET
    "http://localhost:9000/ctfs/range/?from=1437000263744&to=1437000263744"
    {:response-format :json
     :handler (fn [stuff]

                (let [ctf (first (stuff "ctfs"))]
                  (swap! state assoc :ctf ctf)
                  )

                )}
    ))

(defn get-duel []
  (GET
    "http://localhost:9000/duels/range/?from=1436823550599&to=1436823550599"
    {:response-format :json
     :handler         (fn [stuff]
                        (let [duel (first (stuff "duels"))]
                          (set-duel! duel)
                          ;(.log js/console duel)
                          ;(.log js/console (str @state))
                          )
                        )

     }))

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

(defn home-page []
  (do
    [:div

     [:p "This is development-grade software. Don't expect it to work! :-)"]

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
