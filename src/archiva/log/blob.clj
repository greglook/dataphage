(ns archiva.log.blob
  "Data log which keeps data stored in a content-addressable blob store and
  places pointers in a wrapped log."
  (:require
    [blocks.core :as block]
    [archiva.log.core :as log]))


(defrecord BlockDataLog
  [log store]

  log/DataLog

  (create-topic
    [this topic]
    (log/create-topic log topic))


  (list-topics
    [this]
    (log/list-topics log))


  (read-entries
    [this topic start batch]
    (map #(update-in % [:value] (partial block/get store))
         (log/read-entries log topic start batch)))


  (publish!
    [this topic value]
    (let [block (if (instance? blocks.data.Block value)
                  value
                  (block/read! (pr-str value)))
          block (block/put! store block)]
      (assoc
        (log/publish! log topic (:id block))
        :value block))))


(defn block-log
  "Constructs a new data log backed by the given log and block store
  implementations."
  [log store]
  (BlockDataLog. log store))
