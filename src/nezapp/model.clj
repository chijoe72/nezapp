(ns nezapp.model)

(defn login-credentials [user-id username password user-type]
  {:user-id user-id :username username :password password :user-type user-type})

(defn user-info [name surname address mobile-contact email-contact]
  {:name name :surname surname :address address :mobile-contact mobile-contact :email-contact email-contact})

(defn address [user-id street-number street-name suburb]
  {:user-id user-id :street-number street-number :street-name street-name :suburb suburb :city "Cape Town"})

(defn contact [user-id contact contat-type]
  {:user-id user-id :contact contact :contact-type contat-type})

(defn profession [profession-id name]
  {:profession-id profession-id :name name})

(defn quote [send-date subject message sender-uuid receiver-uuid]
  {:send-date send-date :subject subject :message message :sender-uuid sender-uuid :receiver-uuid receiver-uuid})