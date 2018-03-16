(ns fsl-cacm.core-test
  (:require [clojure.test :refer :all]
            [fsl-cacm.core :refer :all]))

(deftest file-name
  (is (= (get-json-file-name-by-time 2018 2 1) "2018-2-1.json")))
