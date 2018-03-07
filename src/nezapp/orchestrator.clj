(ns nezapp.orchestrator
  (:require [matchbox.core :as m]
            [matchbox.async :as ma]
            [clojure.core.async :as async]))

(defn get-user-by-id [user-id]
  (let [root (m/connect "https://nezapp-a4eb4.firebaseio.com")
        users-reference (m/get-in root :users)
        user (async/<!! (ma/deref-list<
                          (m/equal-to (m/order-by-child users-reference :user-id) user-id)
                          ))]
    (if (empty? user)
      :user-not-found
      (nth user 0)
      )
    )
  )

(defn insert-user-login-credentials [user-id user]
  (let [root (m/connect "https://nezapp-a4eb4.firebaseio.com")
        users-reference (m/get-in root :users)]
    (m/reset! users-reference {user-id user})
    )
  )

(defn insert-user-info [user-id user-info]
  (let [root (m/connect "https://nezapp-a4eb4.firebaseio.com")
        users-reference (m/get-in root [:users user-id])
        address-reference (m/get-in root :addresses)
        contact-reference (m/get-in root :contacts)]
    (do
      (m/reset! users-reference  {:name (:name user-info) :surname (:surname user-info)})
      (m/conj! address-reference (:address user-info))
      (m/conj! contact-reference (:mobile-contact user-info) )
      (m/conj! contact-reference (:email-contact user-info))
      )
    )
  )

(defn insert-profession [profession]
  (let [root (m/connect "https://nezapp-a4eb4.firebaseio.com")
        profession-reference (m/get-in root :professions)]
    (m/conj! profession-reference profession)
    )
  )

(defn get-profession []
  (let [root (m/connect "https://nezapp-a4eb4.firebaseio.com")
        profession-reference (m/get-in root :professions)
        professions (async/<!! (ma/deref-list<
                                 profession-reference
                                 ))]
    (if (empty? professions)
      :professions-not-found
      professions
      )
    )
  )