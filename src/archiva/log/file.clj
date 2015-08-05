(ns archiva.log.file
  "File-backed data log."
  (:require
    [archiva.log.core :as log]
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [clojure.string :as str])
  (:import
    java.io.File
    java.util.Date))


(defn- topic->file
  "Returns the log file for the given topic name."
  ^File
  [root topic]
  (io/file root (str topic ".edn")))


(defn- file->topic
  "Converts a file into a topic name."
  [^File file]
  (-> (.getName file)
      (str/replace #"^(.+)\.edn$" "$1")))


(defn- read-edn-values
  [^File file]
  (with-open [in (java.io.PushbackReader. (io/reader file))]
    (->> (repeatedly #(edn/read {:eof ::end} in))
         (take-while (partial not= ::end))
         (doall))))


;; File logs keep a file with an in-order sequence of log entries.
;;
;; <root>/<topic>.edn
(defrecord FileDataLog
  [^File root]

  log/DataLog

  (create-topic
    [this topic]
    (let [file (topic->file root topic)]
      (when-not (.exists file)
        (io/make-parents file)
        (spit file ""))
      nil))


  (list-topics
    [this]
    (locking root
      (->> (.listFiles root)
           (map file->topic)
           (sort))))


  (read-entries
    [this topic start batch]
    (locking root
      (let [file (topic->file root topic)]
        (if (.exists file)
          (->> (read-edn-values file)
               (log/update-entries topic)
               (drop start)
               (take batch))
          (throw (IllegalStateException.
                   (str "Log does not contain topic " topic)))))))


  (publish!
    [this topic value]
    (locking root
      (let [file (topic->file root topic)]
        (if (.exists file)
          (let [entry (log/->entry value)]
            (with-open [out (io/writer file :append true)]
              (.write out (prn-str entry))
              (.flush out))
            (->> (read-edn-values file)
                 (log/update-entries topic)
                 (last)))
          (throw (IllegalStateException.
                   (str "Log does not contain topic " topic))))))))


(defn file-log
  "Constructs a new file-backed data log."
  [root]
  (FileDataLog. (io/file root)))
