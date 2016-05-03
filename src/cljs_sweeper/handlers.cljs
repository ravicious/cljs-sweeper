(ns cljs-sweeper.handlers
  (:require [re-frame.core :as re-frame :refer [dispatch register-handler]]
            [cljs-sweeper.game.core :as game]
            [cljs-sweeper.cell-ui-state :as ui-state]
            [cljs-sweeper.game.move :as move]))

(defn- toggle-visible-cell-displayed-property [cell-ui-state cell]
  (if (:visible cell)
    (ui-state/toggle-displayed-property cell-ui-state)
    cell-ui-state))

(defn- cell-click [db [_ index]]
  (let [cell (game/cell (:game db) index)]
    (-> db
      (update-in [:cell-ui-state index] toggle-visible-cell-displayed-property cell)
      (update-in [:game] move/make index))))

(register-handler
  :cell-click
  cell-click)

(defn- cycle-power-mark [db [_ direction index]]
  (let [powers (get-in db [:game :powers])]
    (update-in db [:cell-ui-state index]
               ui-state/cycle-power-mark powers direction)))

(register-handler
  :cycle-power-mark
  cycle-power-mark)
