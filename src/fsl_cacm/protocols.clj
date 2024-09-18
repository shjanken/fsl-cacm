(ns fsl-cacm.protocols)

(defprotocol ReportDataRepo
  (query [this sld year month] "query data from repo"))

(defprotocol Writer
  (write [this content] "write content, return writed count"))
