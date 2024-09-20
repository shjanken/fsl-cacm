(ns fsl-cacm.report-data.handlers-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [mount.core :as mnt]
   [reitit.ring :as ring]
   [fsl-cacm.report-data.protocols :refer [DataWriter ReportDataRepo write query]]
   [fsl-cacm.report-data.core :as report-data]
   [fsl-cacm.report-data.handlers :as handlers]))

(deftest test-create-file-handler
  (let [mock-repo   (reify ReportDataRepo
                      (query [_this sld year month] {:sld sld :year year :month month}))
        mock-writer (fn [_this _]
                      (reify DataWriter
                        (write [_this content] (count content))))]
    (->
     (mnt/only #{#'report-data/database-report-data-repo
                 #'report-data/data-file-writer})
     (mnt/swap {#'report-data/database-report-data-repo mock-repo
                #'report-data/data-file-writer          mock-writer})
     mnt/start))

  (testing "can create file on the server"
    (let [app (ring/ring-handler (ring/router handlers/api))
          res (app
               {:request-method :put
                :uri "/data/json/01"
                :form-params {:year "2024" :month "01"}})]
      (is (= 200 (:status res)))
      (is (= 3 (:body res)))))

  (mnt/stop))
