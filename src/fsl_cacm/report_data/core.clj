(ns fsl-cacm.report-data.core
  (:require
   [next.jdbc :as jdbc]
   [honey.sql :as sql]
   [honey.sql.helpers :refer [select from]]
   [clj-time.core :as t]
   [clj-time.format :as f]
   [mount.core :refer [defstate]]
   [clojure.java.io :as jio]
   [fmnoise.flow :refer [then else]]
   [fsl-cacm.report-data.protocols :refer [query write ReportDataRepo DataWriter]]
   [fsl-cacm.db :as db]
   [muuntaja.core :as m]))

(def not-found
  {:success false :msg "not found"})

(defn not-found?
  [x]
  (= x not-found))

(defn write-error
  [msg]
  {:success false :msg msg :type :write-error})

(defn write-error?
  [x]
  (= (:type x) :write-error))

(defn invalid-sld
  [sld]
  {:success false :msg (str sld " is not valid") :type :invalid-sld})

(defn invalid-sld?
  [x]
  (= (:type x) :invalid-sld))

(defn save!
  "query the data from repo.
  if data is not nil, write the data to the repo"
  [writer content]
  (let [cnt (write writer content)]
    (cond
      (<= cnt 0) (write-error "no data writtern")
      :else {:success true :msg cnt})))

(defn- check-sld
  [sld]
  (let [sld-sets #{"01" "99"}]
    (contains? sld-sets sld)))

(defn fetch-data
  [repo sld year month]
  (if (check-sld sld)
    (let [data (query repo sld year month)]
      (cond
        (nil? data) not-found
        :else data))
    (invalid-sld sld)))

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

(defn ->sql
  [sld first-day last-day]
  (->
   (select :*)
   (from [[:table [:f_tj_jyyb_2019 first-day last-day sld]]])
   (sql/format :dialect :oracle)))

(defrecord DataBaseReportDataRepo [ds]
  ReportDataRepo
  (query [_this sld year month]
    (let [first-day (->first-day year month)
          last-day  (->last-day year month)
          sql       (->sql sld first-day last-day)]
      (jdbc/execute! ds sql))))

(declare database-report-data-repo)

(defstate database-report-data-repo
  :start (->DataBaseReportDataRepo db/ds))

(defn- path-exists?
  [path]
  (let [path (jio/as-file path)]
    (if (.exists path)
      path
      (ex-info "path not exists" {:path (.getAbsolutePath path)}))))

(defn- path-join
  [path filename]
  (let [data-file (jio/file path filename)]
    (if (.exists data-file)
      (ex-info "file already exists" {:file (.getAbsolutePath data-file)})
      data-file)))

(defn- edn->json
  [content]
  (->>
   content
   (m/encode "application/json")
   slurp))

(defn- write-content
  "write content to the file"
  [file content]
  (spit file content)
  (count content))

(comment

  (->>
   "target"
   (then path-exists?)
   (then #(path-join % "hello.txt"))
   (then #(write-content % "hello world"))
   (else (fn [^Throwable err]
           {:success false :type :write-error
            :msg (.getMessage err)
            :error-data (ex-data err)}))))

(defrecord LocalJsonFileWriter [path file-name]
  DataWriter
  (write [_this content]
    ;; check the path is exists?
    ;; | path is exists
    ;;   | check file is exists
    ;;     | file is exists, return write-error: file already exists
    ;;     | file is not exists, create a file and write the data content
    ;; | path is not exists
    ;;   | return write-error: directory is not exists
    (->>
     path
     (then path-exists?)
     (then #(path-join % file-name))
     (then #(write-content % (edn->json content)))
     (else (fn [^Throwable err]
             (->
              (write-error (ex-message err))
              (assoc :ex-data (ex-data err))))))))

(comment
  (->
   (->LocalJsonFileWriter "target" "hello.txt")
   (write "hello world")))

(declare data-file-writer)

(defstate data-file-writer
  :start #(->LocalJsonFileWriter % %2))

(defn write-data
  "user writer write all data."
  [writer data]
  (write writer data))

(comment
  (require '[fsl-cacm.config :as config])

  (->
   (mount.core/with-args {:profile :dev})
   (mount.core/start #'config/config
                     #'db/ds
                     #'database-report-data-repo))
  (mount.core/stop)

  (fetch-data database-report-data-repo "01" "2024" "02"))

