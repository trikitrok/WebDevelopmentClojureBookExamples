(ns picture-gallery.routes.auth
  (:require [hiccup.form :refer :all]
            [compojure.core :refer :all]
            [picture-gallery.routes.home :refer :all]
            [picture-gallery.views.layout :as layout]
            [noir.session :as session]
            [noir.response :as resp]
            [noir.validation :as validation]
            [noir.util.crypt :as crypt]
            [picture-gallery.models.db :as db]))

(declare registration-page
         handle-registration
         valid?
         error-item
         format-error)

(defroutes auth-routes
           (GET "/register" []
             (registration-page))

           (POST "/register" [id pass pass1]
             (handle-registration id pass pass1)))

(defn control [id label field]
  (list
    (validation/on-error id error-item)
    label
    field
    [:br]))

(defn registration-page [& [id]]
  (layout/base
    (form-to
      [:post "/register"]
      (control
        :id
        (label "user-id" "user id")
        (text-field {:tabindex 1} "id" id))
      (control
        :pass
        (label "pass" "password")
        (password-field {:tabindex 2} "pass"))
      (control
        :pass1
        (label "pass1" "retype password")
        (password-field {:tabindex 3} "pass1"))
      (submit-button {:tabindex 4} "create account"))))

(defn handle-registration [id pass pass1]
  (if (valid? id pass pass1)
    (try
      (db/create-user {:id id :pass (crypt/encrypt pass)})
      (session/put! :user id)
      (resp/redirect "/")
      (catch Exception ex
        (validation/rule false [:id (format-error id ex)])
        (registration-page)))
    (registration-page id)))

(defn valid? [id pass pass1]
  (validation/rule (validation/has-value? id)
                   [:id "user id is required"])
  (validation/rule (validation/min-length? pass 5)
                   [:pass "password must be at least 5 characters"])
  (validation/rule (= pass pass1)
                   [:pass "entered passwords do not match"])
  (not (validation/errors? :id :pass :pass1)))

(defn error-item [[error]]
  [:div.error error])

(defn format-error [id ex]
  (cond
    (and (instance? org.postgresql.util.PSQLException ex)
         (zero? (.getErrorCode ex)))
    (str "The user with id " id " already exists!")

    :else
    "An error occured while processing the request"))