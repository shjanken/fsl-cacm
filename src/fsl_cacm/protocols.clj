(ns fsl-cacm.protocols)

(defprotocol DataRepo
  (query [this sld year month] "query data from repo"))
