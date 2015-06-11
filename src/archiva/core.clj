(ns archiva.core
  "System configuration and setup."
  (:require
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]))


;; ## System Lifecycle

(def system nil)


(defn init!
  "Initialize the system for standalone operation."
  []
  (alter-var-root #'system
    (constantly
      (component/system-map)))
  :init)


(defn configure!
  "Alters the system by loading configuration files."
  []
  ; TODO: load configuration
  :configure)


(defn start!
  "Runs the initialized system."
  []
  (when system
    (log/info "Starting dataphage system...")
    (alter-var-root #'system component/start))
  :start)


(defn stop!
  "Halts the running system."
  []
  (when system
    (log/info "Stopping dataphage system...")
    (alter-var-root #'system component/stop))
  :stop)
