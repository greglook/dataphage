(ns archiva.log.blob
  "Data log which keeps data stored in a content-addressable blob store and
  places pointers in a wrapped log."
  (:require
    [blobble.core :as blob]
    [archiva.log.core :as log]))


(defrecord BlobDataLog
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
    (map #(update-in % [:value] (partial blob/get store))
         (log/read-entries log topic start batch)))


  (publish!
    [this topic value]
    (let [blob (if (instance? blobble.core.Blob value)
                  value
                  (blob/read! (pr-str value)))
          blob (blob/put! store blob)]
      (assoc
        (log/publish! log topic (:id blob))
        :value blob))))


(defn blob-log
  "Constructs a new data log backed by the given log and blob store
  implementations."
  [log store]
  (BlobDataLog. log store))
