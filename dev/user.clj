(ns user
  (:require
   [fsl-cacm.core :as fsl]
   [clojure.tools.namespace.repl :refer [set-refresh-dirs]]
   [integrant.core :as ig]))

(def dev-system (atom nil))

(set-refresh-dirs "src")

(defmethod ig/expand-key :config/config [k v]
  {k (assoc v :profile :dev)})

(defn start-dev-system
  []
  (->
   fsl/system-config
   ig/expand
   ig/init))

(defn stop-dev-system
  []
  (ig/halt! @dev-system))

(comment
  (start-dev-system)

  @dev-system

  (stop-dev-system))
