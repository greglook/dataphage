(ns archiva.source.core-test
  (:require
    [clj-time.core :as time]
    [archiva.source.core :as source]
    [clojure.test :refer :all]))


(deftest coverage-gap-logic
  (let [target (time/interval (time/date-time 2015 1  1)
                              (time/date-time 2015 1 10))]
    (testing "no coverage or limit"
      (let [goals (source/coverage-gaps target nil [])]
        (is (sequential? goals))
        (is (= 1 (count goals)))
        (is (= target (first goals)))))
    (testing "partial coverage, no limit"
      (let [goals (source/coverage-gaps
                    target nil
                    [(time/interval (time/date-time 2015 1 2) (time/date-time 2015 1 4))
                     (time/interval (time/date-time 2015 1 8) (time/date-time 2015 1 9))
                     (time/interval (time/date-time 2015 1 3) (time/date-time 2015 1 6))])]
        (is (= 3 (count goals)))
        (is (= [(time/interval (time/date-time 2015 1 1) (time/date-time 2015 1 2))
                (time/interval (time/date-time 2015 1 6) (time/date-time 2015 1 8))
                (time/interval (time/date-time 2015 1 9) (time/date-time 2015 1 10))]
               goals))))
    (testing "partial coverage, limit period"
      (let [goals (source/coverage-gaps
                    target (time/days 1)
                    [(time/interval (time/date-time 2015 1 3) (time/date-time 2015 1 8))])]
        (is (= 4 (count goals)))
        (is (= [(time/interval (time/date-time 2015 1 1) (time/date-time 2015 1 2))
                (time/interval (time/date-time 2015 1 2) (time/date-time 2015 1 3))
                (time/interval (time/date-time 2015 1 8) (time/date-time 2015 1 9))
                (time/interval (time/date-time 2015 1 9) (time/date-time 2015 1 10))]
               goals))))))
