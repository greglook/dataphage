(defproject mvxcvi/archiva "0.1.0-SNAPSHOT"
  :description "A library for ingesting data from third-party sources."
  :url "https://github.com/greglook/dataphage"
  :license {:name "Public Domain"
            :url "http://unlicense.org/"}

  :dependencies
  [[org.clojure/clojure "1.8.0"]
   [org.clojure/tools.logging "0.3.1"]

   [byte-streams "0.2.2"]
   [ch.qos.logback/logback-classic "1.1.7"]
   [clj-http "3.3.0"]
   [clj-time "0.12.2"]
   [com.stuartsierra/component "0.3.1"]
   [environ "1.1.0"]
   [mvxcvi/blocks "0.8.0"]
   [mvxcvi/withings-clj "0.3.0"]
   [prismatic/schema "1.1.3"]

   [clj-webdriver "0.7.2"]
   [org.seleniumhq.selenium/selenium-java "2.47.0"]
   [com.codeborne/phantomjsdriver "1.3.0"
    :exclusions [org.seleniumhq.selenium/selenium-java
                 org.seleniumhq.selenium/selenium-server
                 org.seleniumhq.selenium/selenium-remote-driver]]]

  :profiles
  {:repl {:source-paths ["dev"]
          :dependencies [[org.clojure/tools.namespace "0.2.10"]]
          :jvm-opts ["-DLOGBACK_APPENDER=repl"
                     "-DAPP_LOG_LEVEL=DEBUG"]}

   :test {:jvm-opts ["-DLOGBACK_APPENDER=nop"
                     "-DAPP_LOG_LEVEL=TRACE"]}})
