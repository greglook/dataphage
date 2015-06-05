(defproject mvxcvi/dataphage "0.1.0-SNAPSHOT"
  :description "A library for ingesting data from third-party sources."
  :url "https://github.com/greglook/dataphage"
  :license {:name "Public Domain"
            :url "http://unlicense.org/"}

  :dependencies
  [[ch.qos.logback/logback-classic "1.1.2"]
   [clj-time "0.9.0"]
   [com.stuartsierra/component "0.2.2"]
   [environ "1.0.0"]
   [org.clojure/clojure "1.7.0-RC1"]
   [org.clojure/tools.logging "0.3.1"]
   [prismatic/schema "0.4.2"]]

  :profiles
  {:repl {:source-paths ["dev"]
          :dependencies [[org.clojure/tools.namespace "0.2.8"]]}})