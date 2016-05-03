(ns cljs-sweeper.utils.conditional-reducers
  (:refer-clojure :exclude [apply]))

(defn apply
  "Given pairs of predicates and reducers, applies the accumulator
  and the additional args to the predicate. If the predicate is true,
  applies the accumulator and the additional args to the reducer."
  [accumulator additional-args & pred-reducers]
  {:pre [(even? (count pred-reducers))]}
  (reduce
    (fn [accumulator [pred reducer]]
      (if (clojure.core/apply pred accumulator additional-args)
        (clojure.core/apply reducer accumulator additional-args)
        accumulator))
      accumulator
      (partition 2 pred-reducers)))

(defn always
  "Used as a predicate for reducers which should be always run."
  [& _]
  true)

(defn acc-only
  "Given f, returns a wrapper which takes any number of args and calls f only with the first argument."
  [f]
  (fn [accumulator & _]
    (f accumulator)))
