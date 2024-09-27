(ns fsl-cacm.core
  (:require
   ;; [fsl-cacm.handlers :refer [app]]
   [mount.core :as m]
   [ring.adapter.jetty :refer [run-jetty]]
   [reitit.ring :as ring]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.coercion :as rrc]
   [muuntaja.core :as muu]
   [fsl-cacm.report-data.handlers :as report-data]
   [fsl-cacm.config :as conf])
  (:gen-class))

(declare app)

(m/defstate app
  :start (ring/ring-handler
          (ring/router
           [report-data/api]
           {:data {:muuntaja muu/instance
                   :middleware [muuntaja/format-middleware
                                rrc/coerce-exceptions-middleware
                                rrc/coerce-request-middleware
                                rrc/coerce-response-middleware]}})))

(declare server)

(m/defstate server
  :start (run-jetty app (conf/server))
  :stop (.stop server))

(defn -main [& _]
  (m/start)
  (println (str "server running on " (:port (conf/server)))))

(comment
  (-main))