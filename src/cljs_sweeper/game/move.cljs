(ns cljs-sweeper.game.move
  (:require [cljs-sweeper.game.core :as game]
            [cljs-sweeper.game.find-safe-indexes-to-reveal :as f]
            [cljs-sweeper.utils.conditional-reducers :as conditional-reducers]))

(defn- surrounding-power-is-zero? [game-state cell-index]
  (let [cell (game/cell game-state cell-index)]
    (zero? (:surrounding-power cell))))

; Custom wrappers for fns which take only the game state
; instead of the game state and the cell index.

(defn- player-power-up? [game-state & _]
  (game/player-power-up? game-state))

(defn- inc-player-power [game-state & _]
  (game/inc-player-power game-state))

(defn- game-over? [game-state & _]
  (game/game-over? game-state))

(defn- end-game [game-state & _]
  (game/end-game game-state))

(defn make [game-state cell-index]
  (let [cell (game/cell game-state cell-index)]
    (if-not (or (:game-over game-state) (:visible cell))
      (conditional-reducers/apply
        game-state [cell-index]
        conditional-reducers/always game/reveal-cell
        surrounding-power-is-zero? game/reveal-safe-cells
        conditional-reducers/always game/attack-player-with-enemy
        player-power-up? inc-player-power
        game-over? end-game)
      game-state)))
