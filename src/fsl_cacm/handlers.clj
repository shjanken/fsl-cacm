(ns fsl-cacm.handlers
  (:require [compojure.core :refer :all]
            [compojure.route :as route]))

(def handler
  (routes
   (GET "/" [] "<h1>Hello world</h1>")
   (route/not-found "<h1>Page not found</h1>")))
