(ns archiva.source.withings
  "Source scrapers for Withings data. Follow the Oauth setup instructions in
  the Withings API docs.

  See: http://oauth.withings.com/api"
  (:require
    [archiva.source.core :as source]
    [clj-time.core :as time]
    [clojure.tools.logging :as log]))


; set up config:
; - existing state
;   - time-coverage intervals
;   - scraping delay (e.g. wait 1 day before trying)
;   - maximum interval per job
; - data directory

; choose a region to parse
; - return nil if everything is already covered
; - otherwise return a _single_ job config (typically, "start at offset 0")

; execute a scraping job
; - takes some config (offset, time windows?)
; - return updated state and data index


; /home/greg/data/source-data/blobs/sha2-256/...
; /home/greg/data/source-data/withings/
;   jobs.edn -> <multihash>
;               <multihash>
;               ...
;   activity-summary.edn -> <multihash>
;                           <multihash>
;                           ...
;   ...




(def topics
  "Set of topics created by the data source."
  #{:body-measurements
    :activity-summary
    :activity-data
    :sleep-summary
    :sleep-data})


(defrecord WithingsDataSource
  [client data-log opts]

  source/DataSource

  (begin-session!
    [this]
    nil)


  (select-jobs
    [this session]
    nil)


  (extract!
    [this session job]
    nil)


  (end-session
    [this session]
    nil))


(defn data-source
  "Constructs a new Withings data source component."
  [client job-store & {:as opts}]
  (WithingsDataSource. client job-store opts))
