(ns cljs-sweeper.game.find-safe-indexes-to-reveal
  (:require [cljs-sweeper.game.board :as b]))

(defn- initialize-indexes-state [current-index]
  {:to-visit #{current-index}
   :visited #{}
   :to-reveal #{}})

(defn- scan-neighbors
  "Determines which neighbors should be visited and which should be revealed."
  [game-state index-state neighbor-indexes]
  (reduce
    (fn [index-state index]
      (let [cell (get-in game-state [:board :cells index])]
        (as-> index-state index-state
            ; Visit neighbor if it's surrounding power equals zero and it hasn't been visited.
            (if (and
                   (zero? (:surrounding-power cell))
                   (not (contains? (:visited index-state) index)))
               (update-in index-state [:to-visit] conj index)
               index-state)
            ; Reveal neighbor if it's power equals zero.
            (if (zero? (:power cell))
               (update-in index-state [:to-reveal] conj index)
               index-state))))
    index-state
    neighbor-indexes))

(defn- scan-current-index-and-neighbors
  "Decides where to put current-index and looks through its neighbors to find other cells to scan."
  [game-state index-state current-index]
  (let [current-cell (get-in game-state [:board :cells current-index])
        neighbor-indexes (b/get-neighbor-indexes-of-index
                           (:board game-state)
                           current-index)]
    (as-> index-state index-state
      ; Add current-index to the list of visited indexes.
      (update-in index-state [:visited] conj current-index)
      ; Determine if the current-cell should be revealed.
      (if (zero? (:power current-cell))
        (update-in index-state [:to-reveal] conj current-index)
        index-state)
      (scan-neighbors
        game-state
        index-state
        neighbor-indexes))))

; The algorith is as follows:
; 1. Remove the first index (later called current-index) from the set of indexes to visit.
; 2. If current-index is nil, that means there are no indexes to check. Return the set of indexes to reveal.
; 3. If current-index is present, add it to the set of visited indexes.
; 4. If current-index power is equal to zero, add it to the set of indexes to reveal.
; 5. For each neighbor of current-index, do the following:
;   a. If the surrounding power of the neighbor is zero and it hasn't been visited, add it
;      to the set of indexes to visit.
;   b. If the power of the neighbor is zero, add it to the set of indexes to reveal.
; 6. Go to 1.

(defn- find-indexes-to-reveal [game-state index-state]
  (let [current-index (first (:to-visit index-state))
        ; Remove current-index from the set of indexes to visit.
        index-state (update-in index-state [:to-visit] (comp set rest))]
    (if-not current-index
      (:to-reveal index-state) ; End evaluation and return the set of indexes to reveal.
      (let [index-state (scan-current-index-and-neighbors game-state index-state current-index)]
        (recur game-state index-state)))))

(defn find-safe-indexes-to-reveal [game-state index-to-process]
  {:pre [(not (nil? index-to-process))]}
  (find-indexes-to-reveal game-state (initialize-indexes-state index-to-process)))
