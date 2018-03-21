(ns fsl-cacm.db-test.product
  (:require [fsl-cacm.db :refer [query-data]]
            [clojure.test :refer :all]))


(deftest query-data-from-product-database
  (let [result (query-data "01" "2018" "2")]
    (is (= (count result) 39))
    (is (= (:jyje (first result)) 44087.91M))))
