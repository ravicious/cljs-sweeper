(ns cljs-sweeper.cell-ui-state)

(defn init [cell]
  {:displayed-property (if (zero? (:power cell))
                         :surrounding-power
                         :power)})

(defn toggle-displayed-property [ui-state]
  (assoc ui-state :displayed-property
         (if (= :power (:displayed-property ui-state))
           :surrounding-power
           :power)))
