(ns nezapp.rest
  (:require
    [taoensso.timbre :as timbre
     :refer [log trace debug info warn error]]
    [compojure.core :refer :all]
    [ring.middleware.params :as ring-params]
    [ring.adapter.jetty :as jetty-server]
    [clojure.string :as str]
    [clojure.data.json :as json]))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Serializable types
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def application-json "application/json")

(defn str->int [s]
  (if s
    (if (number? s)
      s
      (Integer/parseInt (re-find #"\A-?\d+" s)))
    8000))

(defn remove-brackets [var]
  (-> var
      (str/replace-first #"\{" "")
      (str/replace-first #"}" "")))

(defn parse-json [body]
  (try
    (json/read-str (slurp body))
    (catch Exception e
      nil)))

(defn parse-body [content-type body]
  (if body
    (case content-type
      application-json (parse-json body)
      (parse-json body))))

(defn map-results [kk mm]
  (into {}
        (for [[k v] mm]
          [(keyword (str kk "." k)) v])))

(defn get-request-body [{:keys [request-method content-type body] :as request}]
  (parse-body content-type body))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Semantic response helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def headers {"content-type" application-json "access-control-allow-origin" "*" "Access-Control-Allow-Methods" "GET, POST, PATCH, PUT, DELETE, OPTIONS"})
(defn ok [d] {:status 200 :headers headers :body (json/write-str d)})
(defn un-authorised [d] {:status 401 :headers headers :body (json/write-str d)})
(defn bad-request [d] {:status 400 :headers headers :body (json/write-str d)})
(defn forbidden [d] {:status 403 :headers headers :body (json/write-str d)})
(defn not-found [d] {:status 404 :headers headers :body (json/write-str d)})
(defn method-not-allowed [d] {:status 405 :headers headers :body (json/write-str d)})

(defn test-url [compare in]
  "test if two urls match"
  (if (= (count compare) (count in))
    (= (count (remove nil? (mapv (fn [com nn]
                                   (if (false? (str/includes? nn "{"))
                                     (if (false? (= com nn))
                                       false))) compare in))) 0)))

(defn identify-url [{:keys [uri] :as request} routes]
  "finds route given the request and route list"
  (let [request-path-map (str/split (str/replace-first uri #"/" "") #"/")]
    (first (filter (fn [{:keys [uri] :as route}]
                     (if (test-url request-path-map (str/split uri  #"/"))
                       route)) routes))))

(defn identify-url-by-method [{:keys [uri request-method] :as request} routes]
  "finds route given the request and route list"
  (let [request-path-map (str/split (str/replace-first uri #"/" "") #"/")]
    (first (filter (fn [{:keys [uri method] :as route}]
                     (if (test-url request-path-map (str/split uri  #"/"))
                       (if (= request-method method)
                         route))) routes))))

(defn build-path-map [{:keys [uri query-params headers] :as request} matched-route]
  "using all variables in request, this compiles a map of all values [header, path, query, body]"
  (->> (into {} (mapv (fn [rm mm]
                        (if (str/includes? mm "{")
                          {(keyword (str "path." (remove-brackets mm))) rm})) (str/split (str/replace-first uri #"/" "") #"/") (str/split  (:uri matched-route) #"/")))
       (conj (map-results "query" query-params) (map-results "header" headers) (map-results "body" (get-request-body request)))))


(defn build-response [request-data {:keys [function]} data]
  "using given information in endpoint, this compiles message from request and runs function"
  (try
    (let [processed-response (try
                               (function (conj request-data data))
                               (catch Exception e
                                 (error e)
                                 (if (ex-data e)
                                   (let [ext-data (ex-data e)]
                                     (if (:status ext-data)
                                       (if (number? (:status ext-data))
                                         (ex-data e)
                                         {:status 403 :response (ex-data e)})

                                       {:status 403 :response (ex-data e)}))


                                   (if (.getMessage e)
                                     {:status 403 :response {:message (.getMessage e)}}
                                     {:status 503 :response {:message "service-temporarily-unavailable"}}))))]




      (if (:status processed-response)
        (let [headers (if (:headers processed-response)
                        (:headers processed-response)
                        headers)]

          {:status (:status processed-response) :headers headers :body (if (or (map? (:response processed-response)) (vector? (:response processed-response)))
                                                                         (json/write-str (:response processed-response))
                                                                         (:response processed-response))})

        (ok processed-response)))
    (catch Exception e
      (error e)
      {:status 400 :response {:message (.getMessage e)}})))

(defn- clean-uri [uri]
  (->
    (clojure.string/replace uri #"/" "_")
    (clojure.string/replace  #"\{" "")
    (clojure.string/replace  #"}" "")
    (clojure.string/replace  #"-" "")))

(defn check-request [request endoints]
  "rest entry point"
  (do
    (info (str "incoming request : "
               (:request-method request) " "
               (:uri request))
          :uri (:uri request)
          :method (:request-method request)
          :request-header (:headers request))
    (if (= (:request-method request) :options)
      {:status 200 :headers {"access-control-allow-origin" "*" "Access-Control-Allow-Methods" "GET, POST, PATCH, PUT, DELETE, OPTIONS" "Access-Control-Allow-Headers", "X-Requested-With, Content-Type"} :body {}}
      (if endoints
        (let [uri-matched-request (identify-url request endoints)
              matched-request (identify-url-by-method request endoints)]
          (if uri-matched-request
            (if matched-request
              (let [request-data (build-path-map request matched-request)
                    start (System/currentTimeMillis)
                    name-space (clean-uri (:uri matched-request))
                    resp (with-meta
                           (try
                             (build-response request-data
                                             matched-request
                                             (if (:auth matched-request)
                                               ((:auth matched-request) request-data)))
                             (catch Exception e
                               (if (ex-data e)
                                 (un-authorised (ex-data e))
                                 (un-authorised {:message (.getMessage e)}))))
                           {:path name-space})
                    finish-time (System/currentTimeMillis)]
                resp)
              (method-not-allowed {:message "method not allowed"}))
            (not-found {:message "route not found"})))
        (warn "no endpoints defined")))))

(def selected-endpoints nil)

(defn handler [request]
  (let [resp (check-request request selected-endpoints)]
    (if (= "null" (:body resp))
      (do
        (error "Error processing request got nil in body replied with 503 - service-temporarily-unavailable")
        {:status 503 :headers headers :body (json/write-str {:message "service-temporarily-unavailable"})})
      resp)))

(defn innit [port endpoints]
  (info (str "Starting rest endpoints at port: " port))
  (def selected-endpoints endpoints)
  (jetty-server/run-jetty
    (ring-params/wrap-params
      handler)
    {:port  (str->int port)
     :join? false}))