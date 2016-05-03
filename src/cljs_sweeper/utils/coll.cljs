(ns cljs-sweeper.utils.coll
  (:refer-clojure :exclude [shuffle])
  (:require [cljsjs.chance]
            [goog.array :as garray]))

(defn shuffle
  "Given a seed, returns a random permutation of coll. Based on the shuffle
  function from the standard library, with the difference that it passes
  the random number generator to goog.array/shuffle. See:
  https://closure-library.googlecode.com/git-history/docs/namespace_goog_array.html#goog.array.shuffle

  Chance.js was chosen, because it was the first lib that I could find which
  both allowed seeding and had externs for CLJS."
  [seed coll]
  (let [chance (js/Chance. seed)
        a (to-array coll)]
    (garray/shuffle a #(. chance (floating (clj->js {:min 0 :max 1}))))
    (vec a)))

(defn get-next
  "Returns the next item after x in coll. If x is the last item, returns the first item of coll."
  [coll x]
  {:pre [(contains? (set coll) x)]}
  (->> coll cycle (drop-while (partial not= x)) second))

(defn get-prev
  "Returns the previous item before x in coll. If x is the first item, returns the last item of coll."
  [coll x]
  (get-next (rseq coll) x))
