(ns fsl-cacm.repo
  "fsl-cacm.repo is a repository. provider query report data function"
  (:require
   [clj-time.core :as t]
   [clj-time.format :as f]
   [next.jdbc :as jdbc]
   [honey.sql :as sql]
   [honey.sql.helpers :refer [select from]]))

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

(defn- ->last-day
  [year month]
  (get-day year month #'t/last-day-of-the-month))

(comment
  (->last-day "2024" "02")
  (->last-day "2024" "2"))

(defn- ->first-day
  [year month]
  (get-day year month #'t/first-day-of-the-month))

(comment
  (->first-day "2024" "12")
  (->first-day "2024" "1"))

(defn- sql-format
  "create query sql string"
  [sld first-day last-day]
  (->
   (select :*)
   (from [[:table [:f_tj_jyyb_2019 first-day last-day sld]]])
   (sql/format :dialect :oracle)))

(comment
  (sql-format "01" "2024" "07"))

(defrecord Repo [ds])

(defn new
  [db-spec]
  (->Repo (jdbc/get-datasource db-spec)))

(defn query-data
  "Query data from database.
  The data between the first day of year-month and last day of year-month.
  example: `(query-data 01 2018 02)`
    should return the data 20180201-20180228(29) of sld=01"
  [repo sld year month]
  (let [ds        (:ds repo)
        first-day (->first-day year month)
        last-day  (->last-day year month)]
    (->> (sql-format sld first-day last-day)
         (jdbc/execute! ds))))
