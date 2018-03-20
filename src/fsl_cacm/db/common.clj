(ns fsl-cacm.db.common
  (:require [mount.core :refer [defstate]]))

(defn query-string
  ([sld begin end]
   ["select * from table(?,?,?)" sld begin end])
  ([]
   ["select * from jyyb"]))

(defstate db-config
  :start
  {:db-spec {:dbtype "oracle"
             :user "cl"
             :password "cl2008"}
   :query-string query-string})

(comment
  (query-string "01" "2018" "2")
  (query-string)
  )
