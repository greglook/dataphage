(ns archiva.source.schwab
  "Source scrapers for Charles Schwab brokerage data."
  (:require
    [archiva.source.core :as source]
    [clj-time.core :as time]
    [clj-webdriver.taxi :as w]
    [clojure.tools.logging :as log])
  (:import
    org.openqa.selenium.phantomjs.PhantomJSDriver
    org.openqa.selenium.remote.DesiredCapabilities))


(defn phantomjs-driver
  []
  (let [capabilities (doto (DesiredCapabilities.)
                       (.setCapability "phantomjs.page.settings.userAgent" "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0")
                       #_(.setCapability "phantomjs.page.customHeaders.Accept-Language" "en-US")
                       #_(.setCapability "phantomjs.page.customHeaders.Connection" "keep-alive")
                       #_(.setCapability "phantomjs.cli.args"  (into-array String ["--ignore-ssl-errors=true" "--webdriver-loglevel=WARN"])))]
    (clj-webdriver.driver/init-driver {:webdriver (PhantomJSDriver. capabilities)})))


(defn element-labeled-by
  [label-query]
  (let [element-id (w/attribute label-query "for")]
    (w/element {:tag :input, :id element-id})))


(defn login!
  [username password]
  (w/to "https://client.schwab.com/Login/SignOn/CustomerCenterLogin.aspx")
  (w/wait-until #(w/exists? "label#username") 10000)
  (w/input-text (element-labeled-by "label#username") username)
  (w/input-text (element-labeled-by "label#password") password)
  (w/click "a[name=\"btnLogin\"]"))


(defn account-names
  []
  (w/to "https://client.schwab.com/Accounts/Summary/Summary.aspx")
  (mapv w/html (w/elements "table[aria-describedBy=\"brokerageAccount\"] tr.data-row td:first-child a")))
