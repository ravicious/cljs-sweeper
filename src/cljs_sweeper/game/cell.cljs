(ns cljs-sweeper.game.cell)

(defn init [power]
  {:power power
   :visible false
   :surrounding-power nil})
