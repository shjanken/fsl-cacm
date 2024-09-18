(ns fsl-cacm.core-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [mount.core :as m]
   [fsl-cacm.core :as core]
   [fsl-cacm.protocols :refer [ReportDataRepo]]))

(defrecord MockRepo []
  ReportDataRepo
  (query [_repo _sld _year _month] {:data1 "data1" :data2 "data2"})
  (write [_ content] (count content)))

(deftest core-service-test
  (testing "save-data should query the data and write the data"
    (let [saved-count (core/save-report-data! (->MockRepo) "01" "2024" "02")]
      (is (= 2 saved-count)))))
