(ns picture-gallery.routes.auth
  (:require
    [hiccup.form :refer :all]
    [compojure.core :refer :all]
    [picture-gallery.routes.home :refer :all]
    [picture-gallery.views.layout :as layout]
    [noir.session :as session]
    [noir.response :as resp]
    [noir.validation :as validation]
    [noir.util.crypt :as crypt]
    [picture-gallery.models.db :as db]
    [picture-gallery.util :refer [gallery-path]]
    [picture-gallery.routes.upload :refer [delete-image]]
    [noir.util.route :refer [restricted]])
  (:import
    java.io.File))

(declare
  registration-page
  handle-registration
  handle-login
  handle-logout
  delete-account-page
  handle-confirm-delete)

(defroutes auth-routes
  (GET "/register" []
    (registration-page))

  (GET "/logout" []
    (handle-logout))

  (GET "/delete-account" []
    (restricted (delete-account-page)))

  (POST "/register" [id pass pass1]
    (handle-registration id pass pass1))

  (POST "/login" [id pass]
    (handle-login id pass))

  (POST "/confirm-delete" []
    (restricted (handle-confirm-delete))))

(defn error-item [[error]]
  [:div.error error])

(defn control [id label field]
  (list
    (validation/on-error id error-item)
    label
    field
    [:br]))

(defn valid? [id pass pass1]
  (validation/rule (validation/has-value? id)
                   [:id "user id is required"])
  (validation/rule (validation/min-length? pass 5)
                   [:pass "password must be at least 5 characters"])
  (validation/rule (= pass pass1)
                   [:pass "entered passwords do not match"])
  (not (validation/errors? :id :pass :pass1)))

(defn format-error [id ex]
  (cond
    (and (instance? org.postgresql.util.PSQLException ex)
         (zero? (.getErrorCode ex)))
    (str "The user with id " id " already exists!")

    :else
    "An error occured while processing the request"))

(defn create-gallery-path []
  (let [user-path (File. (gallery-path))]
    (when-not (.exists user-path)
      (.mkdirs user-path))))

(defn handle-registration [id pass pass1]
  (if (valid? id pass pass1)
    (try
      (db/create-user {:id id :pass (crypt/encrypt pass)})
      (session/put! :user id)
      (create-gallery-path)
      (resp/redirect "/")
      (catch Exception ex
        (validation/rule false [:id (format-error id ex)])
        (registration-page)))
    (registration-page id)))

(defn handle-login [id pass]
  (let [user (db/get-user id)]
    (if (and user (crypt/compare pass (:pass user)))
      (session/put! :user id)))
  (resp/redirect "/"))

(defn handle-logout []
  (session/clear!)
  (resp/redirect "/"))

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

(defn delete-account-page []
  (layout/common
    (form-to
      [:post "/confirm-delete"]
      (submit-button "delete account"))
    (form-to
      [:get "/"]
      (submit-button "cancel"))))

(defn handle-confirm-delete []
  (let [user (session/get :user)]
    (doseq [{:keys [name]} (db/images-by-user user)]
      (delete-image user name))
    (clojure.java.io/delete-file (gallery-path))
    (db/delete-user user))
  (session/clear!)
  (resp/redirect "/"))