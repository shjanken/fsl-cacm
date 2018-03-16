(ns fsl-cacm.db
  (:require [clojure.java.jdbc :as jdbc]
            [clj-time.core :as t]
            [clj-time.format :as f]))

(def db-spec
  {:classname "org.h2.Driver"
   :subprotocol "h2"
   :subname "./test-db.h2"})

(defn query-data
  "Query data from database.
  The data between the first day of year-month and last day of year-month.
  example: `(query-data 01 2018 02)`
  nshould return the data 20180201-20180228(29) of sld:01"
  [sld year month]
  (let [fist-day (first-day year month)
        last-day (last-day year month)]
    (jdbc/query db-spec ["select * from table(?,?,?)", first-day, last-day, sld])))

(defn- get-day
  "Get the first or last day for the year month"
  [year month fn]
  (let [date-format (f/formatter "yyyyMMdd")
        y (Integer/parseInt year)
        m (Integer/parseInt month)]
    (->>
     (apply fn [y m])
     (f/unparse date-format)
     (str))))

(defn last-day
  [year month]
  (get-day year month #'t/last-day-of-the-month))

(defn first-day
  [year month]
  (get-day year month #'t/first-day-of-the-month))

(comment
  (get-day "2018" "02" #'t/last-day-of-the-month)
  (last-day "2018" "2")
  )
