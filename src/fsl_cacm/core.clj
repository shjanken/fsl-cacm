(ns fsl-cacm.core
  (:require [fsl-cacm.handlers :refer [handler]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn -main
  "run the web server"
  [& args]
  (run-jetty handler {:port 3000}))

(defn get-resource-dir
  []
  "./resources/")

(defn get-json-file-name-by-time
  "expmple: 2018 2 -> 2018-02-01.json"
  [year month sld]
  (str year "-" month "-" sld ".json"))
