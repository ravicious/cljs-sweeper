(ns cljs-sweeper.db
  (:require [re-frame.core :as re-frame :refer [register-handler]]
            [cljs-sweeper.game.core :as game]
            [cljs-sweeper.cell-ui-state :as ui-state]))

(defn- start-game [seed]
  (let [game-state (game/init :16x30 seed)
        cells-state (mapv ui-state/init (game/cells game-state))]
    {:game game-state
     :cell-ui-state cells-state}))

(register-handler
  :initialize-state
  (fn [db _]
    (if (seq db)
      db
      (let [seed (rand-int (.-MAX_SAFE_INTEGER js/Number))]
        (start-game seed)))))

(register-handler
  :start-game
  (fn [db [_ seed]]
    (start-game seed)))
