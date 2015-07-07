(ns archiva.extract.store.memory
  (:require
    [archiva.extract.core :as extract]
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]))


;; Job data and records in a memory store are held in a map in an atom.
(defrecord MemoryBlobStore
  [memory]

  extract/ExtractedDataStore

  (create-job!
    [this job]
    (when-let [id #_ (job-id) nil]
      ; TODO: NYI
      (assoc job :id id)))

  (list-jobs
    [this opts]
    ; TODO: filtering
    (keys @memory))

  (get-job
    [this job-id]
    (get @memory job-id))

  (add-file!
    [this job-id file-path source]
    (swap! memory
      (fn [mem]
        (when-not (contains? mem job-id)
          (throw (IllegalStateException.
                   (str "Store does not contain job " job-id))))
        (when (get-in mem [job-id :files file-path])
          (throw (IllegalStateException.
                   (str "Job " job-id " already contains file " file-path))))
        (assoc-in mem [job-id :files file-path]
                  {:content (slurp source)})))
    ; TODO: return file metadata
    )

  (open-file
    [this job-id file-path]
    ; TODO: NYI
    nil))


(defn memory-store
  "Creates a new in-memory extracted-data store."
  []
  (MemoryBlobStore. (atom (sorted-map) :validator map?)))
