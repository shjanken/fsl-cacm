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
(s/def ::create-file-form-params (s/keys :req-un [::year ::month]))
(s/def ::sld string?)
(s/def ::create-file-path-params (s/keys :req-un [::sld]))

(def api
  ["/data"
   {:middleware [parameters-middleware]}
   ["/json/:sld"
    {:put {:summery    "create a data file in the server"
           :parameters {:path ::create-file-path-params
                        :body ::create-file-form-params}
           :handler    create-data-file}

     :coercion rcs/coercion}]])

