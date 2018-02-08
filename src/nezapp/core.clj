(ns nezapp.core
  (:gen-class)
  (:require
    [nezapp.endpoints :as ep]
    [nezapp.rest :as rest]))

(defn -main
  []
  (rest/innit "1738" ep/endpoints)
  )
