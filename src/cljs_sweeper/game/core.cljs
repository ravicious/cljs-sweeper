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

(defn reveal-cell [game-state cell-index]
  (update-in game-state [:board] b/reveal-cell cell-index))

(defn build-neighbor-reducer-for-game-state [game-state indexes-visited]
  (fn [[acc-to-visit acc-to-reveal] index]
    (let [cell (get-in game-state [:board :cells index])]
      ; visit neighbor if it's surrounding power equals zero
      [(if (and
             (zero? (:surrounding-power cell))
             (not (contains? indexes-visited index)))
         (conj acc-to-visit index)
         acc-to-visit)
       ; reveal neighbor if it's power equals zero
       (if (zero? (:power cell))
         (conj acc-to-reveal index)
         acc-to-reveal)])))

(defn group-neighbor-indexes
  "Determines which neighbors should be visited and which should be revealed"
  [game-state indexes-to-visit indexes-visited indexes-to-reveal neighbor-indexes]
  (let [neighbor-reducer (build-neighbor-reducer-for-game-state game-state indexes-visited)]
    (reduce
      neighbor-reducer
      [indexes-to-visit indexes-to-reveal]
      neighbor-indexes)))

(defn find-safe-indexes-to-reveal
  ([game-state
    indexes-to-visit
    indexes-visited
    indexes-to-reveal]
   (let [current-index (first indexes-to-visit)
         next-indexes-to-visit (rest indexes-to-visit)]
     (if-not current-index
       indexes-to-reveal ; end evaluation and return the set of indexes to reveal
       (let [current-cell (get-in game-state [:board :cells current-index])
             ; add current-index to the list of visited indexes
             final-indexes-visited (conj indexes-visited current-index)
             ; determine if the current-cell should be revealed
             next-indexes-to-reveal (if (zero? (:power current-cell))
                              (conj indexes-to-reveal current-index)
                              indexes-to-reveal)
             neighbor-indexes (b/get-neighbor-indexes-of-index
                                (:board game-state)
                                current-index)
             [final-indexes-to-visit
              final-indexes-to-reveal] (group-neighbor-indexes
                                         game-state
                                         next-indexes-to-visit
                                         final-indexes-visited
                                         next-indexes-to-reveal
                                         neighbor-indexes)]
         (recur game-state
                final-indexes-to-visit
                final-indexes-visited
                final-indexes-to-reveal)))))
  ([game-state index-to-process]
   {:pre [(not (nil? index-to-process))]}
   (find-safe-indexes-to-reveal game-state [index-to-process] #{} #{})))

(defn profile-function [profile-name function & args]
  (do
    (.profile js/console profile-name)
    (let [result (apply function args)]
      (do
        (.profileEnd js/console)
        result))))

(defn reveal-safe-cells [game-state cell-index]
  (let [indexes-to-reveal (profile-function "find-safe-indexes-to-reveal" find-safe-indexes-to-reveal game-state cell-index)]
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
