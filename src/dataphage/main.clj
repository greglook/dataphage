(ns dataphage.main
  "Main entry-point for launching the service."
  (:gen-class)
  (:require
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]
    [environ.core :refer [env]]))


;; ## System Lifecycle

(def system nil)


(defn init!
  "Initialize the system for standalone operation."
  []
  (alter-var-root #'system
    (constantly
      (component/system-map)))
  :init)


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


(defn -main []
  (init!)
  (.addShutdownHook
    (Runtime/getRuntime)
    (Thread. ^Runnable stop! "system shutdown hook"))
  (start!)
  (log/info "System started, entering active mode..."))
