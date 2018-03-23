(ns fsl-cacm.handlers
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer :all]
            [fsl-cacm.db :as db]
            [fsl-cacm.io :as io]
            [ring.util.response :as response]
            [clojure.java.io :as jio]
            [cheshire.core :as json]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.cors :refer [wrap-cors]]))

(declare get-data write-file!)

(def handler
  (routes
   (GET "/data/json/:sld" [sld year month] (get-data sld year month))
   (PUT "/data/json/:sld" [sld year month] (write-file! sld year month))
   (GET "/" [] "<h1>Hello world</h1>")
   (route/not-found "<h1>Page not found</h1>")))

(defn get-data
  "server resource file.
  if file not exists, return 404"
  [sld year month]
  (let [file-resource (str (io/file-path) (io/file-name sld year month))]
    (if (. (jio/file file-resource) exists)
      (->
       (slurp file-resource)
       (json/parse-string)
       (response/response))
      (response/not-found "resources not exists"))))

(defn write-file!
  "query the data from database then write to resource file.
  with effect side"
  [sld year month]
  (let [file-dest (str (io/file-path) (io/file-name sld year month))]
    (io/write-file file-dest
                   (-> 
                    (db/query-data sld year month)
                    (json/generate-string)))
    {:status 200 :body "create file"}))

(def app
  (->
   handler
   (wrap-json-response)
   (wrap-cors :access-control-allow-origin [#"http://www.cacm.com.cn"])
   (wrap-defaults api-defaults)))
