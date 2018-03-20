(ns fsl-cacm.io)

(defn file-name
  "If parameter is sld-01, year-2018, month-2.
  Return 1-2018-2.json "
  [sld year month]
  (str sld "-" year "-" month ".json"))

(defn file-path
  "Retrun the path where json file store"
  []
  (str "./resources/data/"))

(defn write-file
  "Write the file to dest"
  [dest content]
  (spit dest content))

(comment
  (file-name "01" "2018" "2")
  )
