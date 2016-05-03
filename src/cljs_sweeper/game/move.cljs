(ns cljs-sweeper.game.move
  (:require [cljs-sweeper.game.core :as game]
            [cljs-sweeper.game.find-safe-indexes-to-reveal :as f]
            [cljs-sweeper.utils.conditional-reducers :as conditional-reducers]))

(defn- surrounding-power-is-zero? [game-state cell-index]
  (let [cell (game/cell game-state cell-index)]
    (zero? (:surrounding-power cell))))

(defn make [game-state cell-index]
  (let [cell (game/cell game-state cell-index)]
    (if-not (or (:game-over game-state) (:visible cell))
      (conditional-reducers/apply
        game-state [cell-index]

        conditional-reducers/always
        game/reveal-cell

        surrounding-power-is-zero?
        game/reveal-safe-cells

        conditional-reducers/always
        game/attack-player-with-enemy

        (conditional-reducers/acc-only game/player-power-up?)
        (conditional-reducers/acc-only game/inc-player-power)

        (conditional-reducers/acc-only game/game-over?)
        (conditional-reducers/acc-only game/end-game))
      game-state)))
