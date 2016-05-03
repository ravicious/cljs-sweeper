(ns cljs-sweeper.game.player
  (:require [cljs-sweeper.game.cell :as cell]))

(defn init []
  {:health 10
   :power 1
   :exp 0})

(defn inc-power [player]
  (update-in player [:power] inc))

(defn dec-health
  "Decrements player health by given count, but makes sure it doesn't go below zero."
  [player count]
  (update-in player [:health] #(max 0 (- % count))))

(defn inc-exp
  "Given a power, calculates the received exp from the given power and adds it to player exp."
  [player power]
  (let [exp-rewarded (cell/exp-rewarded power)]
    (update-in player [:exp] + exp-rewarded)))

(defn dead? [player]
  (<= (:health player) 0))

(defn attack-enemy [player enemy]
  (if (> (:power enemy) (:power player))
    (dec-health player (:power enemy))
    (inc-exp player (:power enemy))))

(defn power-up? [player exp-progression]
  (when-let [exp-to-next-power (get exp-progression (inc (:power player)))]
    (<= exp-to-next-power (:exp player))))
