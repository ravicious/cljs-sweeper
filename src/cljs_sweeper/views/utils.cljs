(ns cljs-sweeper.views.utils)

(defn classnames [classes-map]
  (reduce
    (fn [classes [class value]]
      (if value
        (str classes " " class)
        classes))
    ""
    classes-map))
