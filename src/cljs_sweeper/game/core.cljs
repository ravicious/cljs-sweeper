(ns cljs-sweeper.game.core
  (:require [cljsjs.chance]
            [cljs-sweeper.game.board :as b]
            [cljs-sweeper.game.player :as p]
            [cljs-sweeper.game.cell :as c]
            [cljs-sweeper.game.find-safe-indexes-to-reveal :as f]
            [cljs-sweeper.utils :as utils]))

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
       (b/get-neighbor-cells-of-index game-variant cells)
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

(defn- init-cells [game-variant seed]
  (let [cell-configuration (:cell-configuration game-variant)
        zero-cells-count (count-zero-cells game-variant)
        full-cell-configuration (assoc cell-configuration 0 zero-cells-count)]
    (->> full-cell-configuration
         (map (fn [[level count]] (repeat count level)))
         flatten
         (utils/shuffle seed)
         (map c/init)
         (update-surrounding-power-in-cells game-variant)
         vec)))

(defn init [game-variant-id seed]
  (if-let [game-variant (game-variant-id game-variants)]
    {:player (p/init)
     :board (b/init
              (:rows game-variant)
              (:columns game-variant)
              (init-cells game-variant seed))
     :seed seed}
    (throw "Invalid game variant ID")))

(defn reveal-cell [game-state cell-index]
  (update-in game-state [:board] b/reveal-cell cell-index))

(defn reveal-safe-cells [game-state cell-index]
  (let [indexes-to-reveal (f/find-safe-indexes-to-reveal game-state cell-index)]
    (reduce
      (fn [game-state index-to-reveal]
        (reveal-cell game-state index-to-reveal))
      game-state
      indexes-to-reveal)))

(defn make-move [game-state cell-index]
  (let [cell (get-in game-state [:board :cells cell-index])
        player (:player game-state)]
    (if-not (:visible cell)
      (cond
        (and
          (zero? (:power cell))
          (zero? (:surrounding-power cell)))
        (-> game-state
            (reveal-safe-cells cell-index))
        (>= (:power player) (:power cell))
        (-> game-state
            (reveal-cell cell-index)
            (update-in [:player :exp] + (:power cell)))
        (< (:power player) (:power cell))
        (-> game-state
            (reveal-cell cell-index)
            (update-in [:player :health] - (:power cell)))
        :else game-state)
      game-state)))
