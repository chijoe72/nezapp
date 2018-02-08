(ns nezapp.model)

(defn login-credentials [user-id username password user-type]
  {:user-id user-id :username username :password password :user-type user-type}
  )

(defn user-info [name surname email cellphone street-number street-name suburb]
  {:name name :surname surname :email email :cellphone cellphone :street-number street-number :street-name street-name :suburb suburb}
  )

(defn address [user-id street-number street-name suburb]
  {:user-id user-id :street-number street-number :street-name street-name :suburb suburb}
  )

(defn contact [user-id contact contat-type]
  {:user-id user-id :contact contact :contact-type contat-type}
  )

(defn profession [profession-id name]
  {:profession-id profession-id :name name}
  )