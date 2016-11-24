(ns archiva.source.withings
  "Source scrapers for Withings data. Follow the Oauth setup instructions in
  the Withings API docs.

  See: http://oauth.withings.com/api"
  (:require
    [archiva.log.core :as dlog]
    [archiva.source.core :as source]
    [clj-time.core :as time]
    [clojure.tools.logging :as log]
    [mvxcvi.withings :as withings]))


; set up config:
; - existing state
;   - time-coverage intervals
;   - scraping delay (e.g. wait 1 day before trying)
;   - maximum interval per job
; - data directory

; choose a region to parse
; - return nil if everything is already covered
; - otherwise return a single job config for each topic

; execute a scraping job
; - takes some config (offset, time windows?)
; - return updated state and data index


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

  (open-session!
    [this]
    ; TODO: use `user-info` to verify credentials
    nil)

  (close-session!
    [this session]
    ; no-op
    nil)

  (plan-jobs
    [this session]
    ; TODO: check enabled topics to pull, generate target intervals and goals
    nil)

  (extract!
    [this session job]
    ; TODO: use the client to fetch each batch of data, save it to the log
    nil))


(defn data-source
  "Constructs a new Withings data source component."
  [client job-store & {:as opts}]
  (WithingsDataSource. client job-store opts))
