(ns fsl-cacm.report-data.handlers
  (:require
   #_[reitit.ring.middleware.muuntaja :as muuntaja]
   #_[reitit.ring.coercion :as rrc]
   [reitit.coercion.spec :as rcs]
   [reitit.ring.middleware.parameters :refer [parameters-middleware]]
   #_[muuntaja.core :as m]
   [clojure.spec.alpha :as s]
   [ring.util.response :as resp]

   [fsl-cacm.report-data.core :as report-data]
   [fsl-cacm.config :as conf]))

(defn create-data-file
  [req]
  (let [sld                  (get-in req [:path-params :sld])
        {:keys [year month]} (get-in req [:parameters :body])
        data                 (report-data/fetch-data report-data/database-report-data-repo sld year month)
        file-name            (str sld "_" year "_" month ".json")
        writer               (report-data/data-file-writer (conf/data-file-path) file-name)]
    (cond
      (report-data/not-found? data)   (resp/not-found data)
      (report-data/invalid-sld? data) (resp/bad-request data)
      :else                           (let [cnt (report-data/write-data writer data)]
                                        (if (report-data/write-error? cnt)
                                          (resp/response cnt)
                                          (resp/response {:success true
                                                          :message (str "write " cnt " to " file-name)}))))))

(defn query-report-data
  [{{:keys [sld year month]} :path-params}]
  (let [file-name (str sld "_" year "_" month ".json")
        repo      (report-data/local-file-report-data-repo (conf/data-file-path) file-name)
        data      (report-data/fetch-data repo sld year month)]
    (cond
      (report-data/not-found? data) (resp/not-found data)
      :else                         (->
                                     (resp/response data)
                                     (resp/header "Content-Type" "application/json")))))

(comment
  (->
   (resp/bad-request "bad request")
   (resp/header "Content-type" "application/html"))

  (require '[mount.core :as mnt])
  (require '[fsl-cacm.report-data.protocols :refer [ReportDataRepo DataWriter]])

  (def mock-repo
    (reify ReportDataRepo
      #_(query [_ sld year month] (str sld "," year "," month))
      (query [_ _sld _year _month] "example datas")))

  (-> (mnt/with-args {:profile :dev})
      (mnt/swap {#'report-data/database-report-data-repo mock-repo
                 #_#_#'report-data/data-file-writer (fn [_ _] mock-writer)})
      mnt/start)

  (mnt/running-states)

  (create-data-file {:parameters {:form-params {:year "2014" :month 12}}
                     :path-params {:sld "01"}})

  (mnt/stop))

(s/def ::year string?)
(s/def ::month string?)
(s/def ::sld string?)

(s/def ::create-file-form-params (s/keys :req-un [::year ::month]))
(s/def ::create-file-path-params (s/keys :req-un [::sld]))
(s/def ::query-report-data-params (s/keys :req-un [::sld ::year ::month]))

(def api
  ["/data/json" {:middleware [parameters-middleware]
                 :coercion   rcs/coercion}
   ["/:sld/:year/:month"
    {:get {:summery   "display report data"
           :handler   query-report-data
           :parameter {:path ::query-report-data-params}}}]
   ["/:sld"
    {:put {:summery    "create a data file in the server"
           :handler    create-data-file
           :parameters {:path ::create-file-path-params
                        :body ::create-file-form-params}}}]])

