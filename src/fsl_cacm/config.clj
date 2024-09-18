(ns fsl-cacm.config
  (:require
   [mount.core :refer [defstate args] :as m]
   [aero.core :refer [read-config]]
   [clojure.java.io :refer [resource]]))

(defn- init-config
  [profile]
  (read-config (resource "config.edn") {:profile profile}))

(declare config)

(defstate config
  :start (init-config (-> (m/args) :profile)))

(defn database
  []
  (:database config))

(comment
  (->
   (m/only #{#'config})
   (m/with-args {:profile :dev})
   m/start)

  config

  (m/stop))
