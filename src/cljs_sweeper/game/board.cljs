(ns cljs-sweeper.game.board)

(defn init [rows columns cells]
  {:pre [(< 1 rows), (< 1 columns), (= (count cells) (* rows columns))]}
  {:rows rows
   :columns columns
   :cells (vec cells)})

(def ^:private min-coordinate 0)

(def ^:private within-bounds?
  (memoize
    (fn [width height {:keys [x y]}]
      (and
        (<= min-coordinate x (dec width))
        (<= min-coordinate y (dec height))))))

(defn- calculate-index
  "Translates 2D coordinates to an index of 1D array. Returns nil if given coordinates are out of bounds."
  [{width :columns height :rows} {:keys [x y] :as coordinates}]
  (when (within-bounds? width height coordinates)
    (+ x (* width y))))

(defn- calculate-coordinates
  "Translates 1D array index to 2D coordinates. Returns nil if given index i is out of bounds."
  [{width :columns height :rows} i]
  (let [coordinates {:x (mod i width)
                     :y (quot i width)}]
    (when (within-bounds? width height coordinates)
      coordinates)))

(def ^:private get-neighbor-coordinates
  (memoize
    (fn [{:keys [x y]}]
      [
       {:x (dec x) :y (dec y)} {:x x :y (dec y)} {:x (inc x) :y (dec y)}
       {:x (dec x) :y y      }                   {:x (inc x) :y y      }
       {:x (dec x) :y (inc y)} {:x x :y (inc y)} {:x (inc x) :y (inc y)}
       ])))

(defn get-neighbor-indexes-of-index
  ([board-bounds index]
   {:pre [(integer? index) (>= index 0)]}
   (let [coordinates (calculate-coordinates board-bounds index)
         neighbor-coordinates (get-neighbor-coordinates coordinates)]
     (->> neighbor-coordinates
          (map (partial calculate-index board-bounds))
          (remove nil?))))
  ([board-bounds cells pred index]
   (filter #(pred (get cells %)) (get-neighbor-indexes-of-index board-bounds index))))

(defn get-neighbor-cells-of-index
  ([board-bounds cells index]
   (map (partial get cells) (get-neighbor-indexes-of-index board-bounds index)))
  ([board-bounds cells pred index]
   (filter pred (get-neighbor-cells-of-index board-bounds cells index))))

(defn reveal-cell [board cell-index]
  (update-in board [:cells cell-index] assoc :visible true))
