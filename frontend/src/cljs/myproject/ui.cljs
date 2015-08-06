(ns myproject.ui
  (:require
    [goog.date.Date]
    [re-com.core :refer [horizontal-bar-tabs datepicker v-box h-box horizontal-tabs input-textarea
                         input-text checkbox]]
    [re-com.datepicker :refer [iso8601->date datepicker-args-desc]]
    [reagent.core :as reagent]
    [cljs-time.format :refer [formatter unparse]]
    [cljs-time.core :refer [now days minus day-of-week]]
    ))

; http://re-demo.s3-website-ap-southeast-2.amazonaws.com/#/introduction

(def types-tabs
  [
   {:id :all :label "All games"}
   {:id :ctf :label "CTF games"}
   {:id :duel :label "Duels"}
   {:id :single :label "Single by ID"}
   {:id :by-id :label "Multiple by ID"}
   ])

(def times-tabs
  [{:id :recent :label "Recent" :type "recent"}
   {:id :first :label "First" :type "first"}
   {:id :dated :label "Dated" :type "dated"}
   ])

(def times-2-tabs
  [{:id :until :label "Until" :type "until"}
   {:id :to :label "To" :type "to"}
   {:id :after :label "After" :type "after"}
   {:id :from :label "From" :type "from"}
   ])

(defn pd [dv]
  (cljs-time.format/parse (cljs-time.format/formatters :date-time-no-ms) dv)
  )

(defn upd [d]
  (cljs-time.format/unparse (cljs-time.format/formatters :date-time-no-ms) d)
  )


(defonce model1 (reagent/atom (minus (now) (days 3))))

(def sample-view
  {:type          :all
   :time          :recent
   :time-2        :to
   :players       "w00p|foxie\n|RB|Honzik1"
   :time-val      "2015-08-01T18:54:35Z"
   :fiter-players false
   :ids           "2015-08-01T18:54:35Z\n2015-08-03T16:52:11Z\n2015-08-03T15:42:41Z"
   :id            "2015-08-01T18:54:35Z"})

(defonce modl (reagent/atom sample-view))

(defn is-filtering [view]
  (let [type (:type view)]
    (or
      (= type :all)
      (= type :ctf)
      (= type :duel)
      )
    )
  )

(defn uri-from-view [view]
  (let
    [type (:type view)]
    (str
      "http://alfa.duel.gg/api"
      (cond
        (is-filtering view)
        (str
          (when (= type :duel) "/duel")
          (when (= type :ctf) "/ctf")
          "/games"
          (cond
            (= (:time view) :recent) "/recent"
            (= (:time view) :first) "/first"
            :else
            (str
              (cond
                (= (:time-2 view) :from) "/from"
                (= (:time-2 view) :to) "/to"
                (= (:time-2 view) :until) "/until"
                (= (:time-2 view) :after) "/after"
                )
              "/"
              (:time-val view)
              )

            )
          "/"

          (when (:filter-players view)
            (let
              [players (clojure.string/split (:players view) "\n")
               encoded-names (map #(.encodeURIComponent js/window %1) players)
               named (map #(str "player=" %1) encoded-names)
               qs (clojure.string/join "&" named)
               ] (str "?" qs)))

          )

        (= type :single) (str "/game/" (:id view) "/")
        (= type :by-id) (str "/games/?"
                             (let [games (clojure.string/split (:ids view) "\n")
                                   encoded (map #(.encodeURIComponent js/window %1) games)
                                   named (map #(str "game=" %1) encoded)
                                   qs (clojure.string/join "&" named)
                                   ] qs)
                             )
        )
      )
    ))

(defn dated-games-view [view]
  [v-box
   :children
   [
    [horizontal-bar-tabs
     :model (:time-2 view)
     :tabs times-2-tabs
     :on-change (fn [x] (swap! modl (fn [y] (assoc-in y [:time-2] x))))
     ]

    [datepicker
     :model (pd
              (:time-val view)
              )
     :disabled? false
     :show-today? true
     :show-weeks? true
     :on-change (fn [x]
                  (.log js/console (upd x))
                  (swap!
                    modl
                    #(assoc-in % [:time-val] (upd x))
                  )
                  )
     ]
    ]
   ]
  )

(defn choose-by-ids-view [view]
  [input-textarea :model (:ids view)
   :on-change (fn [x] (swap! modl #(assoc-in % [:ids] x)))]
  )

(defn choose-single-view [view]
  [input-text :model (:id view) :on-change
   (fn [x]
     (swap! modl #(assoc-in % [:id] x)
     ))
   ])

(defn filtering-games-view [view]
  [v-box
   :children
   [
    [horizontal-bar-tabs
     :model (:time view)
     :tabs times-tabs
     :on-change (fn [x] (swap! modl (fn [y] (assoc-in y [:time] x))))
     ]
    (when (= (:time view) :dated) (dated-games-view view))
    [h-box :children [[checkbox :model (:filter-players view)
                       :on-change (fn [x] (swap! modl (fn [y] (assoc-in y [:filter-players] x))))
                       ] " Filter by player names?"]]
    (when (:filter-players view)
      [input-textarea :model (:players view)
       :on-change (fn [x] (swap! modl (fn [y] (assoc-in y [:players] x))))
       ]
      )
    ]
   ]
  )

(defn whut [view]
  [:div
   [:p
    [:a {:target "_blank" :href (uri-from-view view)} (uri-from-view view)]

    ]
   (pr-str view)
   [v-box
    :children
    [
     [horizontal-bar-tabs
      :model (:type view)
      :tabs types-tabs
      :on-change (fn [x] (swap! modl (fn [y] (assoc-in y [:type] x))))
      ]
     (cond
       (= (:type view) :by-id) (choose-by-ids-view view)
       (= (:type view) :single) (choose-single-view view)
       :else (filtering-games-view view)
       )
     ]
    ]]
  )
