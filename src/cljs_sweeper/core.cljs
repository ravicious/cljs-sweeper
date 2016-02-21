(ns cljs-sweeper.core
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs-sweeper.game.core :as game]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:game (game/init :16x30)}))

(defn classnames [classes-map]
  (reduce
    (fn [classes [class value]]
      (if value
        (str classes " " class)
        classes))
    ""
    classes-map))

(defn render-cell [[index cell]]
  ^{:key index} [:td.game-cell
                 {:class (classnames {"game-cell--visible" (:visible cell)
                                      "game-cell--hidden" (not (:visible cell))
                                      (str "game-cell--surrounding-" (:surrounding-power cell)) true})
                  :on-click #(swap!
                               app-state
                               assoc :game
                               (game/make-move (:game @app-state) index))}
                 (if (:visible cell)
                   (if (and
                         (zero? (:power cell))
                         (zero? (:surrounding-power cell)))
                     ""
                     [:span
                      (str "s" (:surrounding-power cell))
                      [:br]
                      (str "p" (:power cell))])
                   "x")])

(defn group-in-rows [nOfColumns cells]
  (map-indexed vector (partition nOfColumns cells)))

(defn render-row [[index cells]]
  ^{:key index} [:tr cells])

(defn render-table [rows]
  [:table.game-board rows])

(defn render-board [board]
  (->> (:cells board)
       (map-indexed vector)
       (map render-cell)
       (group-in-rows (:columns board))
       (map render-row)
       render-table))

(defn render-player [player]
  [:ul
   [:li (str "Level: " (:power player))]
   [:li (str "Exp: " (:exp player))]
   [:li (str "Health: " (:health player))]])

(defn hello-world []
  [:div (render-board (get-in @app-state [:game :board] @app-state)) (render-player (get-in @app-state [:game :player]))])

(reagent/render-component [hello-world]
                          (. js/document (getElementById "app")))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
