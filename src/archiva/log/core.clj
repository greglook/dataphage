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
  "Creates a new log entry for the given value. The returned map will _not_
  contain a topic name or sequence offset."
  [value]
  {:id (java.util.UUID/randomUUID)
   :time (java.util.Date.)
   :value value})


(defn update-entries
  "Updates a sequence of entries by associating a `:topic` and `:seq` number."
  [topic entries]
  (map #(assoc %2 :topic topic :seq %1)
       (range)
       entries))
