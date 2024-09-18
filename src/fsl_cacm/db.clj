(ns fsl-cacm.db
  (:require
   [next.jdbc :as jdbc]
   #_[clj-time.core :as t]
   #_[clj-time.format :as f]
   [mount.core :refer [defstate]]

   [fsl-cacm.config :as config]))

(declare ds)

(defstate ds
  :start (let [db-spec (config/database)]
           (jdbc/get-datasource db-spec)))

(comment
  (mount.core/start #'config/config #'ds)
  (mount.core/running-states)
  (mount.core/stop))

;; (def spec
;;   {:classname "oracle.jdbc.OracleDriver"
;;    :subprotocol "oracle"
;;    :subname "thin:@10.0.0.203:1521/ora9"
;;    :user "cl"
;;    :password "cl2008"})

;; (defn- get-day
;;   "Get the first or last day for the year month"
;;   [year month fn]
;;   (let [date-format (f/formatter "yyyyMMdd")
;;         y (Integer/parseInt year)
;;         m (Integer/parseInt month)]
;;     (->>
;;      (apply fn [y m])
;;      (f/unparse date-format)
;;      (str))))

;; (defn last-day
;;   [year month]
;;   (get-day year month #'t/last-day-of-the-month))

;; (defn first-day
;;   [year month]
;;   (get-day year month #'t/first-day-of-the-month))

;; (defn query-data
;;   "Query data from database.
;;   The data between the first day of year-month and last day of year-month.
;;   example: `(query-data 01 2018 02)`
;;   nshould return the data 20180201-20180228(29) of sld:01"
;;   [sld year month]
;;   (let [first-day (first-day year month)
;;         last-day (last-day year month)]
;;     (jdbc/query spec ["select * from table(f_tj_jyyb_2019(?, ?, ?))" first-day last-day sld])))

;; (defstate query-data
;;   :start query-data)

;; (comment
;;   (get-day "2018" "02" #'t/last-day-of-the-month)
;;   (last-day "2019" "4")
;;   (first-day "2019" "4")
;;   )
