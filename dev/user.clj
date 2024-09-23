(ns user
  (:require
   [clojure.tools.namespace.repl :refer [refresh]]
   [mount.core :as m]
   [reitit.ring :as ring]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   #_[reitit.ring.middleware.exception :as exception]
   #_[expound.alpha :as expound]
   [reitit.ring.coercion :as rrc]
   #_[reitit.coercion.spec :as rcs]
   [reitit.ring.middleware.dev :refer [print-request-diffs]]
   [muuntaja.core :as muu]

   [fsl-cacm.core :as fsl-cacm]
   [fsl-cacm.report-data.handlers :as report-data]))

#_(defn coercion-error-handler [status]
    (let [printer (expound/custom-printer {:theme :figwheel-theme, :print-specs? false})
          handler (exception/create-coercion-handler status)]
      (fn [exception request]
        (printer (-> exception ex-data :problems))
        (handler exception request))))

(def dev-route-data
  {:data {:muuntaja                    muu/instance
          :reitit.middleware/transform print-request-diffs
          :middleware                  [muuntaja/format-middleware
                                        rrc/coerce-exceptions-middleware
                                        rrc/coerce-request-middleware
                                        rrc/coerce-response-middleware]}})

(defn start-dev-system
  []
  (->
   (m/with-args {:profile :dev})
   (m/swap-states {#'fsl-cacm/app
                   {:start
                    #(ring/ring-handler
                      (ring/router [report-data/api] dev-route-data))}})
   (m/start))
  :ready)

(defn reset []
  (m/stop)
  (refresh)
  (start-dev-system))

(comment
  (reset)
  (m/running-states)
  (fsl-cacm/app {:uri "/data/json/01"
                 :request-method :put
                 :body-params {:year "2024" :month "01"}
                 :headers {:content-type "application/json"}})
  (slurp (:body *1)))
