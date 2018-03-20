(ns fsl-cacm.db-test
  (:require [fsl-cacm.db :refer :all]
            [clojure.test :refer :all]))

;; (facts "get first or last day of month"
;;        (fact "first day"
;;              (first-day "2018" "2") => "20180201")
;;        (fact "last day"
;;              (last-day "2018" "2") => "20180228")
;;        (fact "2016 Feb's last day should be `20180229`"
;;              (last-day "2016" "2") => "20160229"))

(deftest get-first-day
  (are [result year month] (= result (first-day year month))
    "20180201" "2018" "2",
    "20180101" "2018" "1"))

(deftest get-last-day
  (are [result year month] (= result (last-day year month))
    "20180228" "2018" "2",
    "20180131" "2018" "1",
    "20180430" "2018" "4",
    "20160229" "2016" "2"))
