(ns cljs-sweeper.game.core
  (:require [cljs-sweeper.game.random-board :as random-board]
            [cljs-sweeper.game.board :as b]
            [cljs-sweeper.game.player :as p]
            [cljs-sweeper.game.cell :as c]))

(def game-variants {:16x30 {:rows 16
                            :columns 30
                            :cell-configuration {1 33
                                                 2 27
                                                 3 20
                                                 4 13
                                                 5 6}}})

(defn- count-zero-cells [{:keys [cell-configuration rows columns]}]
  (let [count-of-all-cells (* rows columns)
        count-of-non-zero-cells (reduce + (vals cell-configuration))]
    (- count-of-all-cells count-of-non-zero-cells)))

(defn- calculate-surrounding-power [game-variant cells index]
  (->> index
       (b/get-neighbors-of-index game-variant cells)
       (reduce
         #(+ %1 (:power %2))
         0)))

(defn- update-surrounding-power-in-cells [game-variant cells]
  (let [cells-vector (vec cells)]
    (map-indexed
      (fn [index cell]
        (assoc
          cell :surrounding-power
          (calculate-surrounding-power game-variant cells-vector index)))
      cells-vector)))

(defn- init-cells [game-variant]
  (let [cell-configuration (:cell-configuration game-variant)
        zero-cells-count (count-zero-cells game-variant)
        full-cell-configuration (assoc cell-configuration 0 zero-cells-count)]
    (->> full-cell-configuration
         (map (fn [[level count]] (repeat count level)))
         flatten
         shuffle
         (map c/init)
         (update-surrounding-power-in-cells game-variant)
         vec)))

(defn init [game-variant-id]
  (if-let [game-variant (game-variant-id game-variants)]
    {:player (p/init)
     :board (b/init
              (:rows game-variant)
              (:columns game-variant)
              (init-cells game-variant))}
    (throw "Invalid game variant ID")))

(defn make-move [game-state cell-index]
  (let [cell (get-in game-state [:board :cells cell-index 1])
        player (:player game-state)]
    (if-not (:visible cell)
      (if (>= (:power player) (:power cell))
        (-> game-state
            (update-in [:board :cells cell-index 1] assoc :visible true)
            (update-in [:player :exp] + (:power cell)))
        (-> game-state
            (update-in [:board :cells cell-index 1] assoc :visible true)
            (update-in [:player :health] - (:power cell))))
      game-state)))
