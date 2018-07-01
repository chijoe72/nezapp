(ns nezapp.util)

(defn get-key-from-value [coll value]
  (first (filter (comp #{value} coll) (keys coll))))
