(ns archiva.source.withings
  "Source scrapers for Withings data. Follow the Oauth setup instructions in
  the Withings API docs.

  See: http://oauth.withings.com/api"
  (:require
    [clj-http.client :as http]
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]
    [oauth.client :as oauth]))




; set up config:
; - existing state
;   - time-coverage intervals
;   - scraping delay (e.g. wait 1 day before trying)
; - data directory
;   - track ids => track data

; choose a region to parse
; - return nil if everything is already covered
; - otherwise return a _single_ job config (typically, "start at offset 0")

; execute a scraping job
; - takes some config (offset, time windows?)
; - return updated state and data index


#_
(defrecord WithingsDataSource
  [data-dir client opts]

  ???

  )

#_
(defn data-source
  "Constructs a new Withings data source component."
  [data-dir client & {:as opts}]
  (WithingsDataSource. data-dir client opts))
