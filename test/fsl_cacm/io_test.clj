(ns fsl-cacm.io-test
  (:require [clojure.test :refer :all]
            [fsl-cacm.io :refer :all]
            [clojure.java.io :refer [file]]))

(deftest file-operator
  (is (= (file-name "01" "2018" "2") "01-2018-2.json"))
  (is (= (file-path) "./resources/data/")))

(deftest file-write
  (write-file "./resources/test.txt" "hello world")
  (is (= (. (file "./resources/test.txt") exists) true))
  (is (=
       (slurp "./resources/test.txt")
       "hello world"))
  (. (file "./resources/test.txt") delete))
