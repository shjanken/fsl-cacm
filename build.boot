(def project 'fsl-cacm)
(def version "0.1.0-SNAPSHOT")

(set-env! :resource-paths #{"resources" "src"}
          :source-paths   #{"test"}
          :dependencies   '[[org.clojure/clojure "RELEASE"]
                            [adzerk/boot-test "RELEASE" :scope "test"]

                            ;; dependency added by me
                            [ring/ring-core "1.6.3"] ; need by boot-http
                            [ring/ring-defaults "0.3.1"] ; (wrap-defaults handler site-defaults)
                            [compojure "1.6.0"]
                            [pandeiro/boot-http "0.8.3"]
                            [ring/ring-jetty-adapter "1.6.3"] ; for main class. java -jar
                            [clj-time "0.14.2"]
                            [mount "0.1.12"]
                            [ring/ring-json "0.4.0"]
                            [ring-cors "0.1.11"]

                            ;; for database
                            [org.clojure/java.jdbc "0.7.5"]
                            [com.oracle/ojdbc6 "11.2"]
                            [cheshire "5.8.0"]

                            ;; for test
                            [midje "1.9.1" :socpe "test"]
                            ;; [metosin/bat-test "0.4.0"]
                            [com.h2database/h2 "1.4.196" :scope "test"]
                            [ragtime "0.7.2" :scope "test"] ; for migration database
                            [ring/ring-mock "0.3.2"]
                            ])

(require '[pandeiro.boot-http :refer :all])
(require '[adzerk.boot-test :refer [test]])

(task-options!
 aot {:namespace   #{'fsl-cacm.core}}
 pom {:project     project
      :version     "0.1"
      :scm         {:url "https://github.com/yourname/fsl-cacm"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}}
 jar {:main        'fsl-cacm.core
      :file        (str "fsl-cacm-" version "-standalone.jar")}

 serve {:handler 'fsl-cacm.handlers/app
        :reload true
        :port 3000})

(deftask build
  "Build the project locally as a JAR."
  [D dir PATH #{str} "the set of directories to write to (target)."]
  (let [dir (if (seq dir) dir #{"target"})]
    (comp (aot) (pom) (uber) (jar) (target :dir dir))))

(deftask run
  "Run the project."
  [a args ARG [str] "the arguments for the application."]
  (require '[fsl-cacm.core :as app])
  (apply (resolve 'app/-main) args))

(deftask dev
  "Run the server by boot-http.
The server will reload when code changed."
  []
  (comp
   (serve)
   (watch)))

(deftask test-without-product-database
  "This task will test without to connect product database."
  []
  (comp
   (test :exclusions #{'fsl-cacm.db-test.product})))

;; (require '[clojure.java.jdbc :as jdbc])
;; (require '[ragtime.jdbc :as rjdbc])
;; (require '[ragtime.repl :as rag])
;; (require '[mount.core :as mount])
;; (require '[clojure.test :refer [run-all-tests]])
;; (require '[fsl-cacm.db-test])
;; (require '[fsl-cacm.handlers-test])
;; (require '[fsl-cacm.core-test])
;; (require '[fsl-cacm.io-test])
;; (deftask test-with-mock-database
;;   "Test with mock database."
;;   []
;;   (let [h2-spec {:classname "org.h2.Driver"
;;                 :subprotocol "h2"
;;                 :subname "./resources/testdb.h2"}
;;        config {:datastore (rjdbc/sql-database h2-spec)
;;                :migrations (rjdbc/load-resources "migrations")}
;;        query-data (fn [sld year month]
;;                     (jdbc/query h2-spec ["select * from jyyb"]))]
;;     (rag/migrate config)
;;     (mount/start-with {#'fsl-cacm.db/query-data query-data})
;;     ;;    (println (test :exclusions #{'fsl-cacm.db-test.product}))
;;     (run-all-tests #"fsl-cacm.*")
;;     (rag/rollback config)
;;     (mount/stop)))

;; (require '[clojure.java.jdbc :refer [query]])
;; (require '[mount.core :refer [defstate] :as mount])
;; (require '[ragtime.jdbc :as rjdbc])
;; (require '[ragtime.repl :as rag])
;; (deftask use-mock-db
;;   "use mock db to test"
;;   []
;;   (let [h2-spec {:classname "org.h2.Driver"
;;                  :subprotocol "h2"
;;                  :subname "./resources/testdb.h2"}
;;         query-fn (fn [] (query h2-spec ["select * from jyyb"]))
;;         config {:datastore (rjdbc/sql-database h2-spec)
;;                 :migrations (rjdbc/load-resources "migrations")}]
;;     (rag/migrate config)
;;     (mount/start-with-states {#'fsl-cacm.db/db-query-data
;;                               {:start #(query-fn)}})))
