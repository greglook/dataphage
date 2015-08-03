(ns archiva.log
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
    [log topic entry]
    "Adds an entry to the given topic in the log. Returns the sequence number
    for the entry."))
