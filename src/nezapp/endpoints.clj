(ns nezapp.endpoints
  (:require
    [nezapp.orchestrator :as orchestrator]
    [nezapp.model :as model]
    [digest.core :as digest])
  (:import (java.util UUID)))




(def endpoints
  [
   ;-----------------------------------------------------------------------------------------------------------------------------------------------------
   ; Sign up user
   ;-----------------------------------------------------------------------------------------------------------------------------------------------------
   {
    :uri      "register/login-credentials"
    :auth     nil
    :method   :post
    :function (fn [payload]
                (do
                  (let [uuid (.toString (UUID/randomUUID))]
                    (orchestrator/insert-user-login-credentials uuid
                                                                (model/login-credentials
                                                                  uuid
                                                                  (:body.username payload)
                                                                  (digest/md5 (:body.password payload))
                                                                  (:body.user-type payload)
                                                                  ))
                    )

                  {:status 200 :response {:response "SUCCESS"}}
                  )
                )
    }

   ;-----------------------------------------------------------------------------------------------------------------------------------------------------
   ; Sign up user
   ;-----------------------------------------------------------------------------------------------------------------------------------------------------
   {
    :uri      "register/user-info/{user-id}"
    :auth     nil
    :method   :post
    :function (fn [payload]
                (do
                  (orchestrator/insert-user-info (:path.user-id payload) (model/user-info
                                                                           (:body.name payload)
                                                                           (:body.surname payload)
                                                                           (model/address (:path.user-id payload)
                                                                                          (:body.street-number payload)
                                                                                          (:body.street-name payload)
                                                                                          (:body.suburb payload))
                                                                           (model/contact
                                                                             (:path.user-id payload)
                                                                             (:body.cellphone payload)
                                                                             :mobile-number
                                                                             )
                                                                           (model/contact
                                                                             (:path.user-id payload)
                                                                             (:body.email payload)
                                                                             :email
                                                                             )))
                  {:status 200 :response {:response "SUCCESS"}}
                  )
                )
    }

   ;-----------------------------------------------------------------------------------------------------------------------------------------------------
   ; Save user
   ;-----------------------------------------------------------------------------------------------------------------------------------------------------

   {
    :uri      "save/user"
    :auth     nil
    :method   :post
    :function (fn [payload]
                (let [uuid (.toString (UUID/randomUUID))]
                  (orchestrator/insert-user-info uuid
                                                 (model/user-info (:body.name payload)
                                                                  (:body.surname payload)
                                                                  (model/address uuid
                                                                                 (:body.street-number payload)
                                                                                 (:body.street-name payload)
                                                                                 (:body.suburb payload))
                                                                  (model/contact
                                                                    uuid
                                                                    (:body.cellphone payload)
                                                                    :mobile-number
                                                                    )
                                                                  (model/contact
                                                                    uuid
                                                                    (:body.email payload)
                                                                    :email))))
                (do

                  {:status 200 :response {:response "SUCCESS" :uuid uuid}}))
    }

   ;-----------------------------------------------------------------------------------------------------------------------------------------------------
   ; Edit user
   ;-----------------------------------------------------------------------------------------------------------------------------------------------------

   {
    :uri      "edit/user/{uuid}"
    :auth     nil
    :method   :post
    :function (fn [payload]
                (let [uuid (:path.uuid payload)]
                  (orchestrator/update-user-info uuid
                                                 (model/user-info (:body.name payload)
                                                                  (:body.surname payload)
                                                                  (model/address uuid
                                                                                 (:body.street-number payload)
                                                                                 (:body.street-name payload)
                                                                                 (:body.suburb payload))
                                                                  (model/contact
                                                                    uuid
                                                                    (:body.cellphone payload)
                                                                    :mobile-number
                                                                    )
                                                                  (model/contact
                                                                    uuid
                                                                    (:body.email payload)
                                                                    :email))))
                (do

                  {:status 200 :response {:response "SUCCESS"}}))
    }

   ;-----------------------------------------------------------------------------------------------------------------------------------------------------
   ; Insert Professions
   ;-----------------------------------------------------------------------------------------------------------------------------------------------------
   {
    :uri      "insert/profession"
    :auth     nil
    :method   :post
    :function (fn [payload]
                (do
                  (orchestrator/insert-profession (model/profession
                                                    (.toString (UUID/randomUUID))
                                                    (:body.name payload)))
                  {:status 200 :response {:response "SUCCESS"}}
                  )
                )
    }

   ;-----------------------------------------------------------------------------------------------------------------------------------------------------
   ; Get Professions
   ;-----------------------------------------------------------------------------------------------------------------------------------------------------
   {
    :uri      "retrieve/professions"
    :auth     nil
    :method   :get
    :function (fn [payload]
                (let [professions (orchestrator/get-profession)]
                  (if (= professions :professions-not-found)
                    {:status 404 :response {:message "Professions not found"}}
                    {:status 200 :response {:response professions}}
                    )
                  )
                )
    }

   ]
  )
