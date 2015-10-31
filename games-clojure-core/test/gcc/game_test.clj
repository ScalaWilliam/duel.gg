(ns gcc.game-test
  (:require [clojure.test :refer :all]
             [gcc.game :refer [get-team-clan is-clanwar?]]
            ))

(def valid-team {"players" [{"clan" "woop"} {"clan" "woop"}]})
(def team-only-one-player {"players" [{"clan" "woop"}]})
(def mixed-clan-team {"players" [{"clan" "woop"}]})
(def one-clan-one-non-clan {"players" [{"clan" "woop"} {}]})

(deftest team-clan-checks
  (is (= "woop" (get-team-clan valid-team)))
  (is (nil? (get-team-clan team-only-one-player)))
  (is (nil? (get-team-clan mixed-clan-team)))
  (is (nil? (get-team-clan one-clan-one-non-clan)))
  )

(def clanwar-game {"teams" {"evil" {"players" [{"clan" "woop"} {"clan" "woop"}]}
                            "good" {"players" [{"clan" "rb"} {"clan" "rb"}]}}})

(def one-player-different-clan {"teams" {"evil" {"players" [{"clan" "woop"} {"clan" "rb"}]}
                            "good" {"players" [{"clan" "rb"} {"clan" "rb"}]}}})

(def one-player-no-clan {"teams" {"evil" {"players" [{"clan" "woop"} {}]}
                            "good" {"players" [{"clan" "rb"} {"clan" "rb"}]}}})

(def one-vs-two {"teams" {"evil" {"players" [{"clan" "woop"}]}
                            "good" {"players" [{"clan" "rb"} {"clan" "rb"}]}}})

(def non-clan-vs-clan {"teams" {"evil" {"players" [{} {} {}]}
                            "good" {"players" [{"clan" "rb"} {"clan" "rb"}]}}})

(def non-clanned {"teams" {"evil" {"players" [{} {} {}]}
                            "good" {"players" [{} {}]}}})

(deftest clanwar-checks
  (is (is-clanwar? clanwar-game))
  (is (not (is-clanwar? one-player-different-clan)))
  (is (not (is-clanwar? one-player-no-clan)))
  (is (not (is-clanwar? one-vs-two)))
  (is (not (is-clanwar? non-clan-vs-clan)))
  (is (not (is-clanwar? non-clanned))))
