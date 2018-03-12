(ns nezapp.core
  (:gen-class)
  (:require
    [nezapp.endpoints :as ep]
    [nezapp.rest :as rest]
    [environ.core :refer [env]]))

(defn -main
  []
  (rest/innit (env :port) ep/endpoints)
  )
