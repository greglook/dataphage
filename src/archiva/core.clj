(ns archiva.core
  "System configuration and setup."
  (:require
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]))


(def system nil)


(defn start!
  "Runs the initialized system."
  []
  (when system
    (log/info "Starting system...")
    (alter-var-root #'system component/start))
  :start)


(defn stop!
  "Halts the running system."
  []
  (when system
    (log/info "Stopping system...")
    (alter-var-root #'system component/stop))
  :stop)
