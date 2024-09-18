(ns fsl-cacm.report-data.protocols)

(defprotocol ReportDataRepo
  (query [this sld year month] "query data from repo"))

(defprotocol DataWriter
  (write [this data]))
