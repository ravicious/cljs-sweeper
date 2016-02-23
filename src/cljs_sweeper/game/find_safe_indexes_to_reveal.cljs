(ns cljs-sweeper.game.find-safe-indexes-to-reveal
  (:require [cljs-sweeper.game.board :as b]))

(defn- build-neighbor-reducer-for-game-state [game-state indexes-visited]
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

(defn- group-neighbor-indexes
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
