(ns user
  (:require
   [fsl-cacm.core :as fsl]
   [clojure.tools.namespace.repl :refer [set-refresh-dirs]]
   [integrant.core :as ig]))

(def dev-system (atom nil))

(set-refresh-dirs "src")

(defn update-dev-system-config
  [system-config]
  (assoc-in system-config [:config/config :profile] :dev))

(defn start-dev-system
  []
  (->> fsl/system-config
       update-dev-system-config
       ig/init
       (reset! dev-system)))

(defn stop-dev-system
  []
  (ig/halt! @dev-system))

(comment
  (start-dev-system)

  @dev-system

  (stop-dev-system))
