(ns fsl-cacm.report-data.handlers-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [mount.core :as mnt]
   [reitit.ring :as ring]
   [muuntaja.core :as muu]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.coercion :as rrc]
   [fsl-cacm.report-data.protocols :refer [DataWriter ReportDataRepo #_write #_query]]
   [fsl-cacm.report-data.core :as report-data]
   [fsl-cacm.report-data.handlers :as handlers]))

(def app
  (ring/ring-handler
   (ring/router [handlers/api]
                {:data {:muuntaja muu/instance
                        :middleware [muuntaja/format-middleware
                                     rrc/coerce-exceptions-middleware
                                     rrc/coerce-request-middleware
                                     rrc/coerce-response-middleware]}})))

(def base-dev-system
  #{#'report-data/database-report-data-repo
    #'report-data/data-file-writer})

(defn valid-conditions
  [sld year month]
  (and (= sld "01") (= year "2024") (= month "01")))

(defn write-error-conditions
  [sld year month]
  (and (= sld "01") (= year "2024") (= month "02")))

(deftest test-create-file-handler
  (let [mock-repo   (reify ReportDataRepo
                      (query [_this sld year month]
                        (cond (valid-conditions sld year month)       {:sld sld :year year :month month}
                              (write-error-conditions sld year month) "mock write error"
                              :else                                   report-data/not-found)))
        mock-writer (fn [_this _]
                      (reify DataWriter
                        (write [_this content]
                          (cond
                            (= content "mock write error") (report-data/write-error "mock write error")
                            :else (count content)))))
        fetch-body (fn [res] (->> res :body slurp (muu/decode "application/json")))]
    (->
     (mnt/only base-dev-system)
     (mnt/swap {#'report-data/database-report-data-repo mock-repo
                #'report-data/data-file-writer          mock-writer})
     mnt/start)
    (testing "can create file on the server"
      (let [res  (app
                  {:request-method :put
                   :uri            "/data/json/01"
                   :body-params    {:year "2024" :month "01"}
                   :headers        {:content-type "application/json"}})
            body (fetch-body res)]
        (is (= 200 (:status res)))
        (is (= true (:success body)))))

    (testing "should return not-found response if data not found"
      (let [res (app {:request-method :put
                      :uri            "/data/json/01"
                      :body-params    {:year "2025" :month "01"}})
            #_#_body (fetch-body res)
            expect-body "not found"]
        (is (= 404 (:status res)))
        (is (= (:body res) expect-body))))

    (testing "can return write-error"
      (let [res (app {:request-method :put
                      :uri            "/data/json/01"
                      :body-params    {:year "2024" :month "02"}})
            body (fetch-body res)
            expect-body {:success false :msg "mock write error" :type "write-error"}]
        (is (= 400 (:status res)))
        (is (= body expect-body))))
    (mnt/stop)))
