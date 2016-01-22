(ns cljs-sweeper.game.random-board
  (:require [cljs-sweeper.game.cell :as cell]
            [cljs-sweeper.game.board :as board]))

(def max-power 5)
(def min-power 0)

(defn- random-power []
  (+ min-power (rand-int max-power)))

(defn- generate-random-cells [n]
  (repeatedly n #(cell/init (random-power))))

(defn generate [rows columns]
  (board/init rows columns (generate-random-cells (* rows columns))))
