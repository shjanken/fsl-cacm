(ns fsl-cacm.report-data.core-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [fsl-cacm.report-data.core :as report-data]
   [fsl-cacm.report-data.protocols :refer [DataWriter ReportDataRepo]]))

(defrecord MockWriter []
  DataWriter
  (write [_this content] (count content)))

(defrecord MockRepo []
  ReportDataRepo
  (query [_this sld year month]
    (if (and (= sld "01")
             (= year "2024")
             (= month "02"))
      {:qx 12 :hj 36}
      nil)))

(deftest report-data-save-test
  (let [mock-writer (->MockWriter)]
    (testing "should can save data content"
      (let [res (report-data/save! mock-writer "content")]
        (is (= 7 (:msg res)))))

    (testing "return write-error value if content is nil"
      (let [res (report-data/save! mock-writer nil)]
        (is (= report-data/write-error res))))

    (testing "write empty string should return write-error"
      (let [res (report-data/save! mock-writer "")]
        (is (= report-data/write-error res))))))

(deftest report-data-query-test
  (let [mock-repo (->MockRepo)]
    (testing "should can query data from repo"
      (let [data (report-data/fetch-data mock-repo "01" "2024" "02")]
        (is (= {:qx 12 :hj 36} data))))

    (testing "return not-found if data not found"
      (let [data (report-data/fetch-data mock-repo "01" "00" "00")]
        (is (= report-data/not-found data))))

    (testing "return invalid-sld if sld is invalid"
      (let [data (report-data/fetch-data mock-repo "12" "2024" "02")
            err (report-data/invalid-sld "12")]
        (is (= err data))))))
