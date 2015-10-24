(ns archiva.main
  "Main entry-point for launching the service."
  (:gen-class)
  (:require
    (archiva
      [config :as config]
      [core :as core])
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]))


(defn init!
  "Initialize the system for standalone operation."
  []
  (log/info "Initializing system...")
  (alter-var-root #'core/system
    (constantly
      (component/system-map
        #_ ...)))
  :init)


(defn configure!
  "Updates the system by loading configuration files."
  [config]
  (when-not core/system
    (throw (IllegalStateException. "The system is not initialized")))
  (config/load-config config)
  :configure)


(defn -main [& args]
  (init!)
  (.addShutdownHook
    (Runtime/getRuntime)
    (Thread. ^Runnable core/stop! "System Shutdown Hook"))
  (core/start!)
  (log/info "System started, entering active mode..."))
