(ns archiva.log.core
  "Protocol for a persistent log data structure.")


(defprotocol DataLog
  "Protocol for a log which can store data."

  (create-topic
    [log topic]
    "Creates a new topic which entries can be logged to.")

  (list-topics
    [log]
    "List the available topics in the log, along with the number of entries in
    each.")

  (read-entries
    [log topic start batch]
    "Reads a batch of entries from a topic in the log. Returns a sequence of
    entries starting at the given offset, up to the given batch size.")

  (publish!
    [log topic value]
    "Adds a value to the given topic in the log. Returns the generated entry."))


(defn ->entry
  "Creates a new log entry for a value in the given topic. The returned map will
  _not_ contain an `:id`."
  [topic value]
  {:topic topic
   :time (java.util.Date.)
   :id (java.util.UUID/randomUUID)
   :value value})


(defn select-range
  "Helper function to select a range of entries. Returned values will contain
  a `:seq` key giving their index into the sequence."
  [entries start batch]
  (some->>
    (seq entries)
    (map #(assoc %2 :seq %1) (range))
    (drop start)
    (take batch)))
