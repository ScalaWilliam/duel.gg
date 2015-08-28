(ns ^:figwheel-always frontend.core
  (:require
    [reagent.core :as reagent :refer [atom]]
    [ajax.core :refer [GET]]
    ))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(def games-endpoint "http://alfa.duel.gg/api/games/recent/?player=w00p%7CDrakas")

(def players-endpoint "http://alfa.duel.gg/api/players/players/")

(defonce app-state (atom {:text "Hello world!"}))

(defn get-recent-games []
  (GET games-endpoint
    {:response-format :json
     :handler         #(swap! app-state assoc :games %)
     }
    ))

(defn get-players []
  (GET players-endpoint
    {:response-format :json
     :handler         #(swap! app-state assoc :players %)
     }
    ))

(defn hello-world []
  [:div
   [:h1 (:text @app-state)]
   [:div (pr-str @app-state)]
   [:div "It workss!"]
   ])

(defonce load-p (get-players))
(defonce load-g (get-recent-games))

(reagent/render-component [hello-world]
                          (. js/document (getElementById "app")))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )

