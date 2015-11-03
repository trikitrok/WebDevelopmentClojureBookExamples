(ns liberator-service.routes.home
  (:require [compojure.core :refer :all]
            [liberator.core
             :refer [defresource request-method-in]]
            [cheshire.core :refer [generate-string]]
            [noir.io :as io]
            [clojure.java.io :refer [file]]))

(def users (atom ["John" "Jane"]))

(defn- user-in-form-params [context]
  (-> context
      (get-in [:request :form-params])
      (get "user")))

(defn- add-user-action [context]
  (swap! users conj (user-in-form-params context)))

(def ^:private home-html-path "/home.html")

(defresource home
  :available-media-types ["text/html"]

  :exists?
  (fn [_]
    [(io/get-resource home-html-path)
     {::file (file (str (io/resource-path) home-html-path))}])

  :handle-ok
  (fn [_]
    (clojure.java.io/input-stream (io/get-resource home-html-path)))

  :last-modified
  (fn [{{{resource :resource} :route-params} :request}]
    (.lastModified (file (str (io/resource-path) home-html-path)))))

(defresource get-users
  :allowed-methods [:get]
  :handle-ok (fn [_] (generate-string @users))
  :available-media-types ["application/json"])

(defresource add-user
  :method-allowed? (request-method-in :post)
  :post! add-user-action
  :handle-created (fn [_] (generate-string @users))
  :available-media-types ["application/json"])

(defroutes home-routes
  (ANY "/" request home)
  (ANY "/add-user" request add-user)
  (ANY "/users" request get-users))

