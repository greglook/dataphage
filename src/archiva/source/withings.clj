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

  (plan-segments
    [this topic interval]
    (let [start (time/start interval)
          end (time/end interval)
          day-start (time/date-time (time/year start)
                                    (time/month start)
                                    (time/day start))]
      (->>
        (periodic/periodic-seq day-start (time/days 1))
        (partition 2 1)
        (take-while #(time/before? (first %) end))
        (mapv (fn ->plan
                [[seg-start seg-end]]
                {:id (ftime/format ...)
                 :topic topic
                 :interval (time/interval seg-start seg-end)})))))

  (open-session!
    [this]
    ; TODO: use `user-info` to verify credentials
    nil)

  (close-session!
    [this session]
    ; no-op
    nil)

  (extract!
    [this session plans]
    ; TODO: use the client to fetch each batch of data, save it to the log
    nil))


(defn data-source
  "Constructs a new Withings data source component."
  [client job-store & {:as opts}]
  (WithingsDataSource. client job-store opts))
