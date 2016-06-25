(ns cljs-sweeper.views.core
  (:require [re-frame.core :refer [subscribe dispatch]]
            [cljs-sweeper.views.utils :as utils]))

(defn group-in-rows [n-of-columns cells]
  (map-indexed vector (partition n-of-columns cells)))

(defn- key-down-handler [index char-code]
  (case char-code
    65 ; A pressed
    (dispatch [:cycle-power-mark :left index])
    68 ; D pressed
    (dispatch [:cycle-power-mark :right index])
    37 ; <- pressed
    (dispatch [:cycle-power-mark :left index])
    39 ; -> pressed
    (dispatch [:cycle-power-mark :right index])
    nil))

(defn- render-cell-component [index]
  (let [cell-and-ui-state (subscribe [:cell-with-ui-state index])]
    (fn []
      (let [[cell ui-state] @cell-and-ui-state
            displayed-property (:displayed-property ui-state)]
        [:td.game-cell
         {:class (utils/classnames
                   ; Classes with simple conditions.
                   {"game-cell--visible" (:visible cell)
                    "game-cell--hidden" (not (:visible cell))
                    (str "game-cell--surrounding-" (:surrounding-power cell)) (:visible cell)
                    ; More complex classes.
                    "game-cell--zero-power"
                    (and (:visible cell) (zero? (:power cell)))
                    "game-cell--monster"
                    (and (:visible cell) (not (zero? (:power cell))))
                    (str "game-cell--monster-" (name displayed-property))
                    (and (:visible cell) (not (zero? (:power cell))))})
          :on-click #(dispatch [:cell-click index])
          :on-key-down #(key-down-handler index (.-which %))
          :on-mouse-over #(.focus (.-target %))
          :tab-index 1}
         (if (:visible cell)
           (cond
             (and
               (zero? (:power cell))
               (zero? (:surrounding-power cell)))
             ""
             (zero? (:power cell))
             (:surrounding-power cell)
             (not (zero? (:power cell)))
             (displayed-property cell))
           (:power-mark ui-state))]))))

(defn- render-cell [index]
  ^{:key index} [render-cell-component index])

(defn- render-row [[index cells]]
  ^{:key index} [:tr cells])

(defn- render-table [rows]
  [:table.game-board
   [:tbody rows]])

(defn render-board []
  (let [columns (subscribe [:columns])
        number-of-cells (subscribe [:number-of-cells])]
    (fn render-board []
      [:div
       (->> (range @number-of-cells)
            (mapv render-cell)
            (group-in-rows @columns)
            (map render-row)
            render-table)])))

(defn render-player []
  (let [game-info (subscribe [:game-info])]
    (fn render-player []
      [:ul
       [:li (str "Level: " (:power @game-info))]
       [:li (str "Exp: " (:exp @game-info))]
       [:li (str "Health: " (:health @game-info))]
       [:li (str "Seed: " (:seed @game-info))]])))

(defn render-game []
  [:div
   [render-board]
   [render-player]])
