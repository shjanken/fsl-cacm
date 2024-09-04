(ns fsl-cacm.core
  (:require
   [fsl-cacm.repo :as repo]
   ;; [fsl-cacm.handlers :refer [app]]
   [ring.adapter.jetty :refer [run-jetty]]
   [integrant.core :as ig]
   [aero.core :as aero]
   [clojure.java.io :refer [resource as-file]]
   [clojure.string :as str]
   [cheshire.core :as json]
   [ring.util.response :as response]
   [compojure.core :refer [routes GET PUT]]
   [compojure.route :as route]
   [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
   [ring.middleware.json :refer [wrap-json-response]]
   [ring.middleware.cors :refer [wrap-cors]])
  (:gen-class))

;; helper =========================================

(defn file-name
  [sld year month]
  (str sld "-" year "-" month ".json"))

(defn write-content
  [dest content]
  (spit dest content))

;; handler function ==========================
(defn query-data
  "server resource data file.
  if no data file , return 404"
  [path sld year month]
  (let [data-file (str path (file-name sld year month))]
    (if (.exists (as-file data-file))
      (-> data-file slurp json/parse-string response/response)
      (response/not-found (str data-file "not exists")))))

(defn write-data!
  "query the data from repo, then write all data to the json-file"
  [repo json-file sld year month]
  (let [data (repo/query-data repo sld year month)
        json-file (str json-file "/" (file-name sld year month))]
    (write-content json-file (json/generate-string data))
    (response/response (str "create file: " json-file))))

;; integrant system config =====================

(def system-config
  {:config/config       {:profile :default}
   :repo/inst           (ig/ref :config/config)
   :json/path           {:config (ig/ref :config/config)}
   :handler/query-data  {:path (ig/ref :json/path)}
   :handler/write-data! {:repository (ig/ref :repo/inst) :path (ig/ref :json/path)}
   :handler/app         {:query-data-handler  (ig/ref :handler/query-data)
                         :write-data!-handler (ig/ref :handler/write-data!)}
   :jetty/adapter       {:app (ig/ref :handler/app) :config (ig/ref :config/config)}})

(defmethod ig/init-key :config/config [_ {:keys [profile]}]
  (aero/read-config (resource "config.edn") {:profile profile}))

(defmethod ig/init-key :repo/inst [_ config]
  (repo/new (:database config)))

(defmethod ig/init-key :json/path [_ {:keys [config]}]
  (->>  
   config
   :data-file-path
   as-file
   (.getAbsolutePath)))

(defmethod ig/init-key :handler/query-data [_ {:keys [path]}]
  (fn [sld year month] (query-data path sld year month)))

(defmethod ig/init-key :handler/write-data! [_ {:keys [repository path]}]
  (fn [sld year month]
    (let [file (str path "/" (file-name sld year month))]
      (write-data! repository file sld year month))))

(defmethod ig/init-key :handler/app [_ {:keys [query-data-handler write-data!-handler]}]
  (let [routes (routes
                (GET "/data/json/:sld" [sld year month] (query-data-handler sld year month))
                (PUT "/data/json/:sld" [sld year month] (write-data!-handler sld year month))
                (GET "/" [] "Hello World")
                (route/not-found "<h1>Page Not Found</h1>"))]
    (-> routes
        wrap-json-response
        (wrap-defaults api-defaults)
        (wrap-cors :access-control-allow-origin [#".*cacm.com.cn.*"]
                   :access-control-allow-methods [:get :put]))))

(defmethod ig/init-key :jetty/adapter [_ {:keys [app config]}]
  (let [server (:server config)
        port   (:port server)
        join?  (:join? server)]
    (run-jetty app {:port port :join? join?})))

(defmethod ig/halt-key! :jetty/adapter [_ server]
  (.stop server))

(comment
  (def system (ig/init system-config))
  system
  (ig/halt! system))

(comment
  ;; create a dev system
  (def dev-system
    (->
     (assoc system-config :config/config {:profile :dev})
     (ig/init)))

  dev-system

  ;; test handler in the dev system
  (let [fn (:handler/query-data dev-system)]
    (fn "01" "2024" "02"))

  ;; stop dev system
  (ig/halt! dev-system))

;; ===============================================

(defn -main
  "run the web server"
  [& _args]
  (ig/init))
