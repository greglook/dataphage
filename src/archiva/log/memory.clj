(ns archiva.log.memory
  "Memory-backed data log."
  (:require
    [archiva.log.core :as log]))


(defrecord MemoryDataLog
  [storage]

  log/DataLog

  (create-topic
    [this topic]
    (swap! storage update topic #(or % []))
    nil)


  (list-topics
    [this]
    (keys @storage))


  (read-entries
    [this topic start batch]
    (if-let [entries (get @storage topic)]
      (->> entries
           (log/update-entries topic)
           (drop start)
           (take batch))
      (throw (IllegalStateException. (str "Log does not contain topic " topic)))))


  (publish!
    [this topic value]
    (when-not (contains? @storage topic)
      (throw (IllegalStateException. (str "Log does not contain topic " topic))))
    (let [entry (log/->entry value)
          new-mem (swap! storage update topic conj entry)]
      (->> (get new-mem topic)
           (log/update-entries topic)
           (last)))))


(defn memory-log
  "Constructs a new memory-backed data log."
  []
  (MemoryDataLog. (atom (sorted-map))))
