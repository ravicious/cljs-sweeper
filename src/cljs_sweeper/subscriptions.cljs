(ns cljs-sweeper.subscriptions
  (:require [re-frame.core :refer [register-sub]]
            [cljs-sweeper.game.core :as game])
  (:require-macros [reagent.ratom :refer [reaction]]))

(defn- indexed-cells-with-ui-state [db, _]
  (let [game-state (reaction (:game @db))
        cell-ui-state (reaction (:cell-ui-state @db))
        cells (reaction (game/cells @game-state))
        number-of-cells (reaction (game/number-of-cells @game-state))
        indexes (reaction (range @number-of-cells))]
    (reaction (map vector @indexes @cells @cell-ui-state))))

(register-sub
  :indexed-cells-with-ui-state
  indexed-cells-with-ui-state)

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
