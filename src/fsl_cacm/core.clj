(ns fsl-cacm.core
  (:require
   ;; [fsl-cacm.handlers :refer [app]]
   [ring.adapter.jetty :refer [run-jetty]]
   [integrant.core :as ig]
   [aero.core :as aero]
   [clojure.java.io :refer [resource as-file]]
   ;; [clojure.string :as jstr]
   [cheshire.core :as json]
   [ring.util.response :as response]
   [compojure.core :refer [routes GET PUT]]
   [compojure.route :as route]
   [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
   [ring.middleware.json :refer [wrap-json-response]]
   [ring.middleware.cors :refer [wrap-cors]]

   [fsl-cacm.repo :as repo]
   [fsl-cacm.protocols :refer [query write]])
  (:gen-class))

(defn error
  [msg]
  {:success? false :msg msg})

(defn success
  "return success result map. if other is a map, merge the other map to the result map.
  example:
    (success \"result\") => {:success? true :message \"result\" }
    (success \"result\" {:other \"other information\"}) => {:sucess? true :message \"result\" :other \"other infomation\"}"
  [msg & other]
  (let [result-map {:success? true :msg msg}]
    (if (map? other)
      (merge result-map other)
      result-map)))

;;== sevice functions =============================

(defn save-report-data!
  "query the data from repo, then write the data"
  [repo sld year month]
  (let [data (query repo sld year month)]
    (write repo data)))
