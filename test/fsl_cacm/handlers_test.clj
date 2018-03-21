(ns fsl-cacm.handlers-test
  (:require [fsl-cacm.handlers :refer [app]]
            [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [clojure.java.io :refer [file]]))


(deftest test-get-data-not-exists
  (let [res (app (mock/request :get "/data/json/01?year=2018&month=2"))]
    (is (= (:status res) 404))
    (is (= (:body res) "resources not exists!"))))

(deftest test-write-resources-file
  (let [res (app (mock/request :put "/data/json/01?year=2018&month=2"))]
    (is (= (:status res) 200))
    (is (= (:body res) "create file"))
    (is (= (. (file "./resources/data/01-2018-2.json") exists) true)))
  ;; test completed
  ;; delete the file
  (. (file "./resources/data/01-2018-2.json") delete))

(deftest test-get-data-exists
  (let [req-str "/data/json/01?year=2018&month=2"
        res (app (mock/request :put req-str))
        res2 (app (mock/request :get req-str))]
    (is (= (:status res2) 200)))
  (. (file "./resources/data/01-2018-2.json") delete))

(comment
  (app (mock/request :get "/data/json/01?year=2018&month=1"))
  (app (mock/request :put "/data/json/01?year=2018&month=1"))
  )
