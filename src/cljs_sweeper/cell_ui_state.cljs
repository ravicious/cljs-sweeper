(ns cljs-sweeper.cell-ui-state
  (:require [cljs-sweeper.utils.coll :as coll]))

(defn init [cell]
  {:displayed-property (if (zero? (:power cell))
                         :surrounding-power
                         :power)
   :power-mark nil})

(defn toggle-displayed-property [ui-state]
  (assoc ui-state :displayed-property
         (if (= :power (:displayed-property ui-state))
           :surrounding-power
           :power)))

(defn cycle-power-mark
  "Cycles power mark in given direction. Available powers are nil and the given powers bigger than one.
  The player can't set one as the expected power, as it doesn't make much sense."
  [ui-state powers direction]
  (let [available-powers (conj (filterv #(< 1 %) powers) nil)
        cycle-f (case direction
                   :right coll/get-next
                   :left coll/get-prev)]
    (update-in ui-state [:power-mark]
               (partial cycle-f available-powers))))
