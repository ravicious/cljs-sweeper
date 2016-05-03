(ns cljs-sweeper.game.player
  (:require [cljs-sweeper.game.cell :as cell]
            [cljs-sweeper.utils.conditional-reducers :as conditional-reducers]))

(defn init []
  {:health 10
   :power 1
   :exp 0})

(defn dead? [player]
  (<= (:health player) 0))

(def alive? (complement dead?))

(defn stronger-than-enemy? [player enemy]
  (>= (:power player) (:power enemy)))

(def weaker-than-enemy? (complement stronger-than-enemy?))

(defn power-up? [player exp-progression]
  (when-let [exp-to-next-power (get exp-progression (inc (:power player)))]
    (<= exp-to-next-power (:exp player))))

(defn- hit-with-enemy
  "Decrements player health by given count, but makes sure it doesn't go below zero."
  [player enemy]
  (let [health-decremented (cell/health-decremented enemy)]
    (update-in player [:health] #(max 0 (- % health-decremented)))))

(defn- inc-exp
  "Given a power, calculates the received exp from the given power and adds it to player exp."
  [player enemy]
  (let [exp-rewarded (cell/exp-rewarded enemy)]
    (update-in player [:exp] + exp-rewarded)))

(defn inc-power [player]
  (update-in player [:power] inc))

(defn attack-enemy [player enemy]
  (conditional-reducers/apply
    player [enemy]
    weaker-than-enemy? hit-with-enemy
    ; We have to always award the player the exp (unless they're dead).
    ; Otherwise there could be a situation in which they aren't able to level up,
    ; because they missed the exp points from a more powerful enemy.
    (conditional-reducers/acc-only alive?) inc-exp))
