(ns cljs-sweeper.core
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs-sweeper.game.core :as game]
            [cljs-sweeper.cell-ui-state :as ui-state]))

(enable-console-print!)

(defonce app-state (atom {}))

(defn start-game [seed]
  (let [game-state (game/init :16x30 seed)
        cells-state (mapv ui-state/init (game/cells game-state))]
    (reset! app-state {:game game-state
                       :cell-ui-state cells-state})))

(if-not (seq @app-state)
  (let [initial-seed (rand-int (.-MAX_SAFE_INTEGER js/Number))]
    (start-game initial-seed)))

(set! (.-startGame js/window) start-game)

(defn classnames [classes-map]
  (reduce
    (fn [classes [class value]]
      (if value
        (str classes " " class)
        classes))
    ""
    classes-map))

(defn cell-click [index]
  (let [cell (game/cell (:game @app-state) index)]
    (do
      (when (:visible cell)
        (swap! app-state update-in [:cell-ui-state index] ui-state/toggle-displayed-property))
      (swap! app-state assoc :game
             (game/make-move (:game @app-state) index)))))

(defn render-cell [index cell ui-state]
  (let [displayed-property (:displayed-property ui-state)]
  ^{:key index} [:td.game-cell
                 {:class (classnames {"game-cell--visible" (:visible cell)
                                      "game-cell--hidden" (not (:visible cell))
                                      "game-cell--zero-power" (and (:visible cell) (zero? (:power cell)))
                                      "game-cell--monster" (and (:visible cell) (not (zero? (:power cell))))
                                      (str "game-cell--monster-" (name displayed-property)) (and (:visible cell) (not (zero? (:power cell))))
                                      (str "game-cell--surrounding-" (:surrounding-power cell)) (:visible cell)})
                  :on-click #(cell-click index)}
                 (when (:visible cell)
                   (cond
                     (and
                         (zero? (:power cell))
                         (zero? (:surrounding-power cell)))
                     ""
                     (zero? (:power cell))
                     (:surrounding-power cell)
                     (not (zero? (:power cell)))
                     (displayed-property cell)))]))

(defn group-in-rows [n-of-columns cells]
  (map-indexed vector (partition n-of-columns cells)))

(defn render-row [[index cells]]
  ^{:key index} [:tr cells])

(defn render-table [rows]
  [:table.game-board rows])

(defn render-board [app-state]
  (let [game-state (:game app-state)
        cells (game/cells game-state)
        cell-ui-state (:cell-ui-state app-state)]
  (->> (map render-cell (range (count cells)) cells cell-ui-state)
       (group-in-rows (game/columns game-state))
       (map render-row)
       render-table)))

(defn render-player [{:keys [player seed]}]
  [:ul
   [:li (str "Level: " (:power player))]
   [:li (str "Exp: " (:exp player))]
   [:li (str "Health: " (:health player))]
   [:li (str "Seed: " seed)]])

(defn hello-world []
  (fn []
    [:div
     [render-board @app-state]
     [render-player (:game @app-state)]]))

(reagent/render-component [hello-world]
                          (. js/document (getElementById "app")))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
