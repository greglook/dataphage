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
    (binding [*ns* (find-ns 'dataphage.config)]
      (if (.isDirectory file)
        (doseq [f (file-seq file)]
          (when (clj-file? f)
            (load-file (str f))))
        (load-file path)))))


(defn component
  "Registers a component in the system map."
  ([k v]
   (alter-var-root #'core/system assoc k v))
  ([k v deps]
   (component k (component/using v deps))))


(defn components
  "Registers a collection of components in the system map. May be passed a map
  of keys to components, or a sequence of key/component pairs."
  [& args]
  (let [comps (if (and (= 1 (count args))
                       (map? (first args)))
                (first args)
                (apply array-map args))]
     (alter-var-root #'core/system merge comps)))
