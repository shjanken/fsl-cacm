(def project 'fsl-cacm)
(def version "0.1.0-SNAPSHOT")

(set-env! :resource-paths #{"resources" "src"}
          :source-paths   #{"test"}
          :dependencies   '[[org.clojure/clojure "RELEASE"]
                            [adzerk/boot-test "RELEASE" :scope "test"]

                            ;; dependency added by me
                            [ring/ring-core "1.6.3"] ; need by boot-http
                            [ring/ring-defaults "0.3.1"] ; (wrap-defaults handler site-defaults)
                            [compojure "1.6.0"]
                            [pandeiro/boot-http "0.8.3"]
                            [ring/ring-jetty-adapter "1.6.3"] ; for main class. java -jar
                            [clj-time "0.14.2"]

                            ;; for database
                            [org.clojure/java.jdbc "0.7.5"]
                            [com.oracle/ojdbc8 "8"]

                            ;; for test
                            [midje "1.9.1" :socpe "test"]
                            [com.h2database/h2 "1.4.196" :scope "test"]
                            [ragtime "0.7.2" :scope "test"] ; for migration data
                            ])

(require '[pandeiro.boot-http :refer :all])
(require '[adzerk.boot-test :refer [test]])

(task-options!
 aot {:namespace   #{'fsl-cacm.core}}
 pom {:project     project
      :version     0.1
      :scm         {:url "https://github.com/yourname/fsl-cacm"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}}
 jar {:main        'fsl-cacm.core
      :file        (str "fsl-cacm-" version "-standalone.jar")}

 serve {:handler 'fsl-cacm.handlers/handler
        :reload true
        :port 3000})

(deftask build
  "Build the project locally as a JAR."
  [d dir PATH #{str} "the set of directories to write to (target)."]
  (let [dir (if (seq dir) dir #{"target"})]
    (comp (aot) (pom) (uber) (jar) (target :dir dir))))

(deftask run
  "Run the project."
  [a args ARG [str] "the arguments for the application."]
  (require '[fsl-cacm.core :as app])
  (apply (resolve 'app/-main) args))

(deftask dev
  "Run the server by boot-http.
The server will reload when code changed."
  []
  (comp
   (serve)
   (watch)))
