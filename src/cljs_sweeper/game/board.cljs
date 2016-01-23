(ns cljs-sweeper.game.board)

(defn init [rows columns cells]
  {:pre [(< 1 rows), (< 1 columns), (= (count cells) (* rows columns))]}
  {:rows rows
   :columns columns
   :cells (vec (map-indexed vector cells))})

(def ^:private min-coordinate 0)

(defn- within-bounds? [width height {:keys [x y]}]
  (and
    (<= min-coordinate x (dec width))
    (<= min-coordinate y (dec height))))

(defn calculate-index
  "Translates 2D coordinates to an index of 1D array. Returns nil if given coordinates are out of bounds."
  [{width :columns height :rows} {:keys [x y] :as coordinates}]
  (when (within-bounds? width height coordinates)
    (+ x (* width y))))

(defn calculate-coordinates
  "Translates 1D array index to 2D coordinates. Returns nil if given index i is out of bounds."
  [{width :columns height :rows} i]
  (let [coordinates {:x (mod i width)
                     :y (quot i width)}]
    (when (within-bounds? width height coordinates)
      coordinates)))

(defn- get-neighbor-coordinates [{:keys [x y]}]
  [
   {:x (dec x) :y (dec y)} {:x x :y (dec y)} {:x (inc x) :y (dec y)}
   {:x (dec x) :y y      }                   {:x (inc x) :y y      }
   {:x (dec x) :y (inc y)} {:x x :y (inc y)} {:x (inc x) :y (inc y)}
   ]
  )

(defn get-neighbors-of-index [board-bounds cells index]
  (let [coordinates (calculate-coordinates board-bounds index)
        neighbor-coordinates (get-neighbor-coordinates coordinates)]
    (->> neighbor-coordinates
         (map (partial calculate-index board-bounds))
         (remove nil?)
         (map (partial get cells)))))

(defn reveal-cell [board cell-index]
  (update-in board [:cells cell-index 1] assoc :visible true))
