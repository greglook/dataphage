(ns user
  (:require
    (archiva
      [core :as core :refer [system start! stop!]]
      [main :as main :refer [init! configure!]])
    (clj-time
      [coerce :as coerce-time]
      [core :as time]
      [format :as format-time])
    [clojure.java.io :as io]
    [clojure.repl :refer :all]
    [clojure.stacktrace :refer [print-cause-trace]]
    [clojure.string :as str]
    [clojure.tools.namespace.repl :refer [refresh]]
    [com.stuartsierra.component :as component]
    [environ.core :refer [env]]))


(defn go!
  "Initializes and starts the system."
  []
  (init!)
  (configure!)
  (start!))


(defn reload!
  "Reloads all changed namespaces to update code, then re-launches the system."
  []
  (stop!)
  (refresh :after 'user/go!))
