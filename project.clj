(defproject mvxcvi/archiva "0.1.0-SNAPSHOT"
  :description "A library for ingesting data from third-party sources."
  :url "https://github.com/greglook/dataphage"
  :license {:name "Public Domain"
            :url "http://unlicense.org/"}

  :dependencies
  [[byte-streams "0.2.0"]
   [ch.qos.logback/logback-classic "1.1.3"]
   [clj-http "2.0.0"]
   [clj-time "0.11.0"]
   [compojure "1.4.0"]
   [com.stuartsierra/component "0.3.0"]
   [environ "1.0.1"]
   [mvxcvi/withings-clj "0.3.0"]
   [org.clojure/clojure "1.7.0"]
   [org.clojure/tools.logging "0.3.1"]
   [prismatic/schema "1.0.1"]
   [ring/ring-core "1.4.0"]
   [ring/ring-jetty-adapter "1.4.0"]]

  :profiles
  {:repl {:source-paths ["dev"]
          :dependencies [[org.clojure/tools.namespace "0.2.10"]]
          :jvm-opts ["-DLOGBACK_APPENDER=repl"
                     "-DAPP_LOG_LEVEL=DEBUG"]}

   :test {:jvm-opts ["-DLOGBACK_APPENDER=nop"
                     "-DAPP_LOG_LEVEL=TRACE"]}})
