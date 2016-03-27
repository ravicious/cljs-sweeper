(ns cljs-sweeper.subscriptions
  (:require [re-frame.core :refer [register-sub]]
            [cljs-sweeper.game.core :as game])
  (:require-macros [reagent.ratom :refer [reaction]]))

(defn- cell-with-ui-state [db, [_, i]]
  (let [cell (reaction (game/cell (:game @db) i))
        ui-state (reaction (get-in @db [:cell-ui-state i]))]
    (reaction [@cell @ui-state])))

(register-sub
  :cell-with-ui-state
  cell-with-ui-state)

(defn- game-info [db, _]
  (let [player (reaction (get-in @db [:game :player]))
        seed (reaction (get-in @db [:game :seed]))]
    (reaction (assoc @player :seed @seed))))

(register-sub
  :game-info
  game-info)

(defn- columns [db, _]
  (reaction (get-in @db [:game :board :columns])))

(register-sub
  :columns
  columns)

(defn- number-of-cells [db, _]
  (reaction (game/number-of-cells (:game @db))))

(register-sub
  :number-of-cells
  number-of-cells)
