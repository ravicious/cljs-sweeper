(ns cljs-sweeper.game.board)

(defn init [rows columns cells]
  {:pre [(< 1 rows), (< 1 columns), (= (count cells) (* rows columns))]}
  {:rows rows
   :columns columns
   :cells (vec (map-indexed vector cells))})

(defn reveal-cell [board cell-index]
  (update-in board [:cells cell-index 1] assoc :visible true))
