(ns fsl-cacm.db-test.mock
  (:require [clojure.java.jdbc :as jdbc]
            [mount.core :as mount]
            [ragtime.jdbc :as rjdbc]
            [ragtime.repl :as rag]
            [clojure.test :refer :all]))

(declare h2-spec)

(defn query-data
  [sld year month]
  (jdbc/query (h2-spec) ["select * from jyyb"]))

(defn start-mock-db!
  []
  (let [h2-spec {:classname "org.h2.Driver"
                 :subprotocol "h2"
                 :subname "./resources/testdb.h2"}
        config {:datastore (rjdbc/sql-database h2-spec)
                :migrations (rjdbc/load-resources "migrations")}]
    (mount/start-with {#'fsl-cacm.db/query-data query-data})
    (rag/migrate config)))

(defn close-mock-db!
  []
  (let [h2-spec {:classname "org.h2.Driver"
                 :subprotocol "h2"
                 :subname "./resources/testdb.h2"}
        config {:datastore (rjdbc/sql-database h2-spec)
                :migrations (rjdbc/load-resources "migrations")}]
    (mount/stop)
    (rag/migrate config)))


(defn h2-spec
  []
   {:classname "org.h2.Driver"
    :subprotocol "h2"
    :subname "./resources/testdb.h2"})

(comment
  (h2-spec)
  )
