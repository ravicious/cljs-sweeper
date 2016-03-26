(ns cljs-sweeper.core
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :as re-frame]
            [cljs-sweeper.game.core :as game]
            [cljs-sweeper.cell-ui-state :as ui-state]
            [cljs-sweeper.handlers]
            [cljs-sweeper.subscriptions]
            [cljs-sweeper.db]
            [cljs-sweeper.views.core :as views]))

(enable-console-print!)

(re-frame/dispatch [:initialize-state])

(set!
  (.-startGame js/window)
  (fn [seed]
    (re-frame/dispatch [:start-game seed])))

(reagent/render-component
  [views/render-game]
  (. js/document (getElementById "app")))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
