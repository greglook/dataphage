(ns archiva.core-test
  (:require
    (archiva.log
      [core :as log]
      [memory :refer [memory-log]]
      [file :refer [file-log]])
    [clojure.java.io :as io]
    [clojure.test :refer :all]))


(defn test-data-log
  "Tests a data log implementation."
  [log label]
  (println "  *" label)
  (testing (.getSimpleName (class log))
    (is (empty? (log/list-topics log))
        "Log starts out with no topics")
    (is (thrown? Exception (log/read-entries log "foo" 0 1))
        "Reading a nonexistent topic throws an exception")
    (let [topic (str (gensym "topic"))]
      (is (nil? (log/create-topic log topic))
          "Successfully created topic returns nil")
      (is (= [topic] (log/list-topics log))
          "New topic shows up in topic list")
      (is (empty? (log/read-entries log topic 0 1))
          "New topic starts empty")
      (let [entry (log/publish! log topic :foo)]
        (is (instance? java.util.UUID (:id entry)))
        (is (instance? java.util.Date (:time entry)))
        (is (= topic (:topic entry)))
        (is (= :foo (:value entry)))
        (is (zero? (:seq entry)))
        )
      (let [[entry :as entries] (log/read-entries log topic 0 5)]
        (is (= 1 (count entries))
            "Topic contains a single entry")
        (is (instance? java.util.UUID (:id entry)))
        (is (instance? java.util.Date (:time entry)))
        (is (= topic (:topic entry)))
        (is (= :foo (:value entry)))
        (is (zero? (:seq entry)))))))



;; ## Log Implementations

(deftest test-memory-log
  (let [subject (memory-log)]
    (test-data-log subject "memory-log")))


(deftest test-file-log
  (let [tmpdir (io/file "target" "test" "tmp"
                        (str "file-data-log."
                          (System/currentTimeMillis)))
        subject (file-log tmpdir)]
    (test-data-log subject "file-log")
    ; TODO: remove tmpdir
    ))
