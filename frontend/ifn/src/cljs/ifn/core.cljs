(ns ifn.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              ;[ajax.core :refer [GET POST]]
              )
    (:import goog.History))

;; -------------------------
;; Views

(defonce ata (atom nil))

(defn the-game []
  (js->clj (.parse js/JSON (.-data @ata))))

(defn render-ctf-team [ctf team]
  [:div.team
   (team "flags") " - "
   (team "name")
   [:ul.players
   (for [player (team "players")]
     ; odd, still get warnings from this, hmm.
     ^{:key (player "name")} [:li
        [:a {:href
             (str "http://duel.gg/player/?name=" (player "name"))
             } (player "name")]
      ]
     )
    ]
   ]
  )

(defn render-ctf [ctf]
  [:div.ctf
   [:h2 [:a {:href (ctf "startTimetext") :target "_blank"}
         (ctf "mode") " @ " (ctf "map")]]
   [:h2 (ctf "startTimeText")]
   [:h3 (ctf "serverDescription")]
   [:div.teams
   (for [team ["good" "evil"]]
     ^{:key team} (render-ctf-team ctf (get-in ctf ["teams" team]))
     )
    ]
   ]
  )

(defn home-page []
  [:div [:h2 "Welcome to ifn"]
   [:div [:a {:href "#/about"} "go to about page"]

    ]
   [:div
    (render-ctf (the-game))
    ]])

(defn about-page []
  [:div [:h2 "About ifn"]
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
  (reagent/render [current-page] (.getElementById js/document "app")))

(def kinds ["live-duel" "live-ctf" "live-duel-gone" "live-ctf-gone" "duel" "ctf"])

(defn woot [k]
  (do
    (swap! ata #(if (nil? %) k %))
    (.log js/console "Godxt it" k)))

(defn load-thingy! []
  (let [es (new js/EventSource "http://api.duel.gg/live-games/all/")]
    (doseq [et kinds]
      (.addEventListener es et #(woot %) false)
      )
    ))

;(defonce
;  load-ctf-game
;  (GET "http://api.duel.gg/game/2015-11-01T18:06:35Z/" :handler (fn [resp] (reset! ctf-game (:body resp))))
;  )

(defn init! []
  (load-thingy!)
  (hook-browser-navigation!)
  (mount-root)
  )
