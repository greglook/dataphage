(ns archiva.main
  "Main entry-point for launching the service."
  (:gen-class)
  (:require
    [archiva.core :as core]
    [clojure.tools.logging :as log]))


(defn -main []
  (log/info "System initializing...")
  (core/init!)
  (.addShutdownHook
    (Runtime/getRuntime)
    (Thread. ^Runnable core/stop! "system shutdown hook"))
  (core/start!)
  (log/info "System started, entering active mode..."))
