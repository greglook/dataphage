(ns archiva.source.utils
  "Utilities for sources."
  (:require
    [clj-time.core :as time]))


(defn- goal-interval
  "Creates an interval from the given time `a` towards a target time `b`. If
  `limit` is not nil, the interval returned will be at most `limit` long."
  [a b limit]
  (time/interval a (if limit (time/earliest b (time/plus a limit)) b)))


(defn- goal-finder
  "Helper for `coverage-gaps` which constructs a function to find goal
  intervals in a target. The function consumes and emits a 3-tuple of
  `[goal-interval? time-marker covering-intervals?]`."
  [target limit]
  (fn next-goal-state
    [[_ t covers]]
    ; Check for end of target interval.
    (when (time/before? t (time/end target))
      (if-let [cover (first covers)]
        ; Check next covering interval.
        (if (time/before? t (time/start cover))
          ; Need to cover up to the start of the next interval.
          (let [goal (goal-interval t
                       (time/earliest (time/start cover) (time/end target))
                       limit)]
            ; Did we make it to the next covered interval?
            (if (time/abuts? goal cover)
              ; Yes! Advance to the end of the next cover.
              [goal (time/end cover) (rest covers)]
              ; Nope. Advance to the end of the goal, keep cover.
              [goal (time/end goal) covers]))
          ; Time is inside the covering interval. Advance to the end of the
          ; cover if it's past the current time.
          [nil (time/latest t (time/end cover)) (rest covers)])
        ; No covering intervals, try to reach for the end of the target.
        (let [goal (goal-interval t (time/end target) limit)]
          [goal (time/end goal) nil])))))


(defn coverage-gaps
  "Calculates a sequence of intervals necessary to cover the desired interval,
  taking into account the collection of already-covered intervals. The
  resulting intervals will not be longer than the `limit` period, if set. If
  the target is already completely covered, returns an empty list."
  [target limit coverage]
  {:pre [(some? target)]}
  (->> [nil (time/start target) (sort-by time/start coverage)]
       (iterate (goal-finder target limit))
       (take-while some?)
       (keep first)))
