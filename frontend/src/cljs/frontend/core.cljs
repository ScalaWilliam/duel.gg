(ns frontend.core
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [goog.events :as events]
            [cljs.core.async :refer [put! <! >! chan timeout]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs-http.client :as http]))

(enable-console-print!)

(defn fetch-widgets
  [url]
  (let [c (chan)]
    (go (let [{widgets :body} (<! (http/get url))]
          (>! c (vec widgets))))
    c))

(def app-state
  (atom {}))

(defn widget [w owner opts]
  (om/component
   (dom/li nil (get w :simpleId))))

(defn widget-list [{:keys [widgets]}]
  (om/component
   (apply dom/ul nil
          (om/build-all widget widgets))))

(defn widget-box [app owner opts]
  (reify
    om/IWillMount
    (will-mount [_]
                ;; Initialize :widgets to []
                (om/transact! app [:widgets] (fn [] []))
                ;; Every opts{:poll-interval} milliseconds get :widgets from server
                (go (while true
                      (let [widgets (<! (fetch-widgets (:url opts)))]
                        (.log js/console widgets)
                        (om/update! app [:widgets] widgets))
                      (<! (timeout (:poll-interval opts))))))
    om/IRender
    (render [_]
            (dom/div nil
                     (dom/h1 nil "Widgets")
                     (dom/h2 nil "LELLEL")
                     (om/build widget-list app)))))

(defn om-app [app owner]
  (reify
    om/IRender
    (render [_]
            (dom/div nil
                     (om/build widget-box app
                               {:opts {:url "/duels"
                                       :poll-interval 2000}})))))

(om/root om-app app-state
         {:target (. js/document (getElementById "content"))})

