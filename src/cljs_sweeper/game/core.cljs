(ns cljs-sweeper.game.core
  (:require [cljsjs.chance]
            [cljs-sweeper.game.board :as b]
            [cljs-sweeper.game.player :as p]
            [cljs-sweeper.game.cell :as c]
            [cljs-sweeper.game.find-safe-indexes-to-reveal :as f]
            [cljs-sweeper.utils.coll :as coll]))

(defn cell [game-state index]
  (get-in game-state [:board :cells index]))

(defn cells [game-state]
  (get-in game-state [:board :cells]))

(defn columns [game-state]
  (get-in game-state [:board :columns]))

(defn rows [game-state]
  (get-in game-state [:board :rows]))

(defn player [game-state]
  (:player game-state))

(defn number-of-cells [game-state]
  (* (columns game-state) (rows game-state)))

(def ^:private game-variants
  {:16x30 {:rows 16
           :columns 30
           :cell-configuration {1 33
                                2 27
                                3 20
                                4 13
                                5 6}
           :exp-progression {2 10
                             3 50
                             4 167
                             5 271}}})

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
         (coll/shuffle seed)
         (map c/init)
         (update-surrounding-power-in-cells game-variant)
         vec)))

(def ^:private powers
  "Given a game-variant, returns vector with non-zero powers of given game variant."
  (comp vec sort keys :cell-configuration))

(defn init [game-variant-id seed]
  (if-let [game-variant (game-variant-id game-variants)]
    {:player (p/init)
     :board (b/init
              (:rows game-variant)
              (:columns game-variant)
              (init-cells game-variant seed))
     :powers (powers game-variant)
     :seed seed
     :game-over false
     :exp-progression (:exp-progression game-variant)}
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

(defn player-power-up? [game-state]
  (p/power-up? (player game-state) (:exp-progression game-state)))

(defn inc-player-power [game-state]
  (update game-state :player p/inc-power))

(defn attack-player-with-enemy [game-state cell-index]
  (update game-state :player p/attack-enemy (cell game-state cell-index)))

(defn game-over? [game-state]
  (p/dead? (player game-state)))

(defn end-game [game-state]
  (assoc game-state :game-over true))
