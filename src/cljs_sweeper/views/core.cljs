(ns cljs-sweeper.views.core
  (:require [re-frame.core :refer [subscribe dispatch]]
            [cljs-sweeper.views.utils :as utils]))

(defn group-in-rows [n-of-columns cells]
  (map-indexed vector (partition n-of-columns cells)))

(defn- render-cell [[index cell ui-state]]
  (let [displayed-property (:displayed-property ui-state)]
    ^{:key index} [:td.game-cell
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
                    :on-click #(dispatch [:cell-click index])}
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

(defn- render-row [[index cells]]
  ^{:key index} [:tr cells])

(defn- render-table [rows]
  [:table.game-board rows])

(defn render-board []
  (let [indexed-cells-with-ui-state (subscribe [:indexed-cells-with-ui-state])
        columns (subscribe [:columns])]
    (fn render-board []
      [:div
       (->> @indexed-cells-with-ui-state
            (map render-cell)
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
