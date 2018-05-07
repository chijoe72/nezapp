(ns nezapp.orchestrator
  (:require [matchbox.core :as m]
            [matchbox.async :as ma]
            [clojure.core.async :as async]))

(defn get-user-by-id [user-id]
  (let [root (m/connect "https://nezapp-a4eb4.firebaseio.com")
        users-reference (m/get-in root [:users user-id])
        user (async/<!! (ma/deref<
                          users-reference))]
    (if (empty? user)
      :user-not-found
      user)))

(defn get-address-by-userid [user-id]
  (let [root (m/connect "https://nezapp-a4eb4.firebaseio.com")
        address-reference (m/get-in root :addresses)
        address (async/<!! (ma/deref-list<
                             (m/equal-to (m/order-by-child address-reference :user-id) user-id)
                             ))]
    (if (empty? address)
      :user-not-found
      (first address))))

(defn get-profile-photo-by-userid [user-id]
  (let [root (m/connect "https://nezapp-a4eb4.firebaseio.com")
        profile-photo-reference (m/get-in root :profilephoto)
        profile-photo (async/<!! (ma/deref-list<
                                   (m/equal-to (m/order-by-child profile-photo-reference :uuid) user-id)
                                   ))]
    (if (empty? profile-photo)
      :user-not-found
      profile-photo)))

(defn get-contact-by-userid [user-id]
  (let [root (m/connect "https://nezapp-a4eb4.firebaseio.com")
        contact-reference (m/get-in root :contacts)
        contact (async/<!! (ma/deref-list<
                             (m/equal-to (m/order-by-child contact-reference :user-id) user-id)
                             ))]
    (if (empty? contact)
      :user-not-found
      contact)))

(defn get-professional []
  (let [root (m/connect "https://nezapp-a4eb4.firebaseio.com")
        professional-reference (m/get-in root :professionals)
        professional (async/<!! (ma/deref-list<
                                  professional-reference
                                  ))]
    (if (empty? professional)
      :professional-not-found
      professional)))

(defn get-work-photos-by-userid [user-id]
  (let [root (m/connect "https://nezapp-a4eb4.firebaseio.com")
        work-photos-reference (m/get-in root :workphotos)
        work-photos (async/<!! (ma/deref-list<
                                 (m/equal-to (m/order-by-child work-photos-reference :uuid) user-id)
                                 ))]
    (if (empty? work-photos)
      :work-photos-not-found
      work-photos)))

(defn get-profession-by-profession-id [profession-id]
  (let [root (m/connect "https://nezapp-a4eb4.firebaseio.com")
        profession-reference (m/get-in root :professions)
        profession (async/<!! (ma/deref-list<
                                (m/equal-to (m/order-by-child profession-reference :profession-id) profession-id)
                                ))]
    (if (empty? profession)
      :profesion-not-found
      (first profession))))

(defn get-professional-profession-by-user-id [user-id]
  (let [root (m/connect "https://nezapp-a4eb4.firebaseio.com")
        professional-profession-reference (m/get-in root :professional-profession)
        professional-profession (async/<!! (ma/deref-list<
                                             (m/equal-to (m/order-by-child professional-profession-reference :user-id) user-id)
                                             ))]
    (if (empty? professional-profession)
      :profesional-profession-not-found
      professional-profession)))

(defn insert-user-login-credentials [user-id user]
  (let [root (m/connect "https://nezapp-a4eb4.firebaseio.com")
        users-reference (m/get-in root :users)]
    (m/reset! users-reference {user-id user})))

(defn insert-user-info [user-id user-info]
  (let [root (m/connect "https://nezapp-a4eb4.firebaseio.com")
        users-reference (m/get-in root [:users user-id])
        address-reference (m/get-in root :addresses)
        contact-reference (m/get-in root :contacts)]
    (m/reset! users-reference {:name (:name user-info) :surname (:surname user-info)})
    (m/conj! address-reference (:address user-info))
    (m/conj! contact-reference (:mobile-contact user-info))
    (m/conj! contact-reference (:email-contact user-info))))

(defn insert-professional-profession [professional-profession]
  (let [root (m/connect "https://nezapp-a4eb4.firebaseio.com")
        professional-profession-reference (m/get-in root :professional-profession)]
    (m/conj! professional-profession-reference professional-profession)))

(defn insert-professional [professional]
  (let [root (m/connect "https://nezapp-a4eb4.firebaseio.com")
        professionals-reference (m/get-in root :professionals)]
    (m/conj! professionals-reference professional)))

(defn save-professional-profession [professions]
  (mapv (fn [profession]
          (do
            (insert-professional-profession {:profession-id (get profession "profession-id") :user-id (get profession "user-id")})
            (insert-professional {:user-id (get profession "user-id")}))) professions))

(defn update-address [id address]
  (let [root (m/connect "https://nezapp-a4eb4.firebaseio.com")
        address-reference (m/get-in root [:addresses id])]
    (m/merge! address-reference address)))

(defn update-contact [id contact]
  (let [root (m/connect "https://nezapp-a4eb4.firebaseio.com")
        contact-reference (m/get-in root [:contacts id])]
    (m/merge! contact-reference contact)))


(defn update-user-info [user-id user-info]
  (let [root (m/connect "https://nezapp-a4eb4.firebaseio.com")
        users-reference (m/get-in root [:users user-id])]
    (m/merge! users-reference {:name (:name user-info) :surname (:surname user-info)})
    (update-address (subs (str (key (get-address-by-userid user-id))) 1) (:address user-info))
    (let [contact (get-contact-by-userid user-id)]
      (mapv (fn [m]
              (when (= (:contact-type (val m)) :email)
                (update-contact (subs (str (key m)) 1) (:email-contact user-info)))
              (when (= (:contact-type (val m)) :mobile-number)
                (update-contact (subs (str (key m)) 1) (:mobile-contact user-info))))
            contact))))

(defn insert-profession [profession]
  (let [root (m/connect "https://nezapp-a4eb4.firebaseio.com")
        profession-reference (m/get-in root :professions)]
    (m/conj! profession-reference profession)))

(defn get-profession []
  (let [root (m/connect "https://nezapp-a4eb4.firebaseio.com")
        profession-reference (m/get-in root :professions)
        professions (async/<!! (ma/deref-list<
                                 profession-reference
                                 ))]
    (if (empty? professions)
      :professions-not-found
      professions)))

(defn get-professionals []
  (let [professionals (get-professional)]
    (if (= :professional-not-found professionals)
      :professionals-not-found
      (mapv (fn [professional]
              (let [user-id (:user-id professional)
                    user (get-user-by-id user-id)
                    contact (get-contact-by-userid user-id)
                    mobile-number (let [mobile-contact (first (filter #(= (:contact-type %) :mobile-number) contact))]
                                    (if (nil? mobile-contact) nil (:contact mobile-contact)))
                    email (let [email-contact (first (filter #(= (:contact-type %) :email) contact))]
                            (if (nil? email-contact) nil (:contact email-contact)))
                    address (get-address-by-userid user-id)
                    profile-photo (first (get-profile-photo-by-userid user-id))
                    work-photos (let [images (get-work-photos-by-userid user-id)]
                                  (mapv (fn [image]
                                          (:name image))
                                        images))
                    professional-profession (get-professional-profession-by-user-id user-id)
                    professions (mapv (fn [m]
                                        (let [profession (get-profession-by-profession-id (:profession-id m))]
                                          (:name profession)))
                                      professional-profession)
                    professional-response (atom {
                                                 :name          (:name user)
                                                 :surname       (:surname user)
                                                 :street-name   (:street-name address)
                                                 :street-number (:street-number address)
                                                 :suburb        (:suburb address)
                                                 :city          (:city address)
                                                 :professions   professions})]
                (do
                  (if (not (nil? mobile-number))
                    (swap! professional-response #(conj % {:mobile-number mobile-number})))
                  (if (not (nil? email))
                    (swap! professional-response #(conj % {:email email})))
                  (if (not (nil? work-photos))
                    (swap! professional-response #(conj % {:work-photos work-photos})))
                  (if (not (nil? profile-photo))
                    (swap! professional-response #(conj % {:profile-photo (:name profile-photo)}))))))
            professionals))))