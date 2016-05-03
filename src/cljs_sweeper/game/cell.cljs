(ns cljs-sweeper.game.cell)

(defn init [power]
  {:power power
   :visible false
   :surrounding-power nil})

(defn exp-rewarded [{:keys [power]}]
  (if (> power 2)
    (. js/Math (pow 2 (- power 1)))
    power))

(defn health-decremented [{:keys [power]}]
  (if (> power 1)
    (* power (dec power))
    1))
