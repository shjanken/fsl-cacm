(ns fsl-cacm.db-test
  (:require [fsl-cacm.db :refer :all]
            [ragtime.jdbc :as rj]
            [ragtime.repl :as r]
            [midje.sweet :refer :all]))

(facts "get the `first/last` day of month"
       (fact "last day"
             (last-day "2018" "01") => "20180131")
       (fact "first day"
             (first-day "2018" "01") => "20180101")
       (fact "2016 Feb have 29 days"
             (last-day "2016" "02") => "20160229"))
