(ns archiva.log.memory
  "Memory-backed data log."
  (:require
    [archiva.log :as log]))


(defrecord MemoryDataLog
  [storage]

  log/DataLog

  (create-topic
    [this topic]
    (swap! storage update-in [topic] #(or % [])))


  (list-topics
    [this]
    (keys @storage))


  (read-entries
    [this topic start batch]
    (if-let [entries (get @storage topic)]
      (take batch (drop start entries))
      (throw (IllegalStateException. (str "Log does not contain topic " topic)))))


  (publish!
    [this topic entry]
    (let [new-mem (swap! storage update-in [topic] conj entry)]
      (count (get new-mem topic)))))


(defn memory-log
  "Constructs a new memory-backed data log."
  []
  (MemoryDataLog. (atom (sorted-map))))
