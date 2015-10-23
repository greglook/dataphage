(ns archiva.config
  "Namespace for loading configuration files."
  (:require
    [archiva.core :as core]
    [clojure.java.io :as io]
    [com.stuartsierra.component :as component]
    [environ.core :refer [env]])
  (:import
    java.io.File))


(defn- clj-file?
  "Determines whether the given file is a clojure file."
  [^File file]
  (and (.isFile file)
       (.matches (.getName file) ".*\\.clj$")))


(defn load-config
  "Load a config file or directory. If the path points to a directory, all
  files with names ending in `.clj` within it will be loaded recursively."
  [path]
  (let [file (io/file path)]
    (binding [*ns* (find-ns 'archiva.config)]
      (if (.isDirectory file)
        (doseq [f (file-seq file)]
          (when (clj-file? f)
            (load-file (str f))))
        (load-file path)))))


(defn defsource
  "Registers a data-source component in the system map."
  ([k src]
   (alter-var-root #'core/system assoc k src))
  ([k src deps]
   (defsource k (component/using src deps))))
