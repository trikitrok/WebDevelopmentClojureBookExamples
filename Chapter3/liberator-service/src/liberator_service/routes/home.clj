(ns liberator-service.routes.home
  (:require [compojure.core :refer :all]
            [liberator.core
             :refer [defresource request-method-in]]
            [cheshire.core :refer [generate-string]]
            [noir.io :as io]
            [clojure.java.io :refer [file]]))

(def users (atom ["John" "Jane"]))

(defn- extract-user [context]
  (-> context
      (get-in [:request :form-params])
      (get "user")))

(defn- add-user-action [context]
  (swap! users conj (extract-user context)))

(def ^:private home-html-path "/home.html")

(defn- empty-user-name? [context]
  (empty? (extract-user context)))

(defn- exist-file? [path]
  [(io/get-resource path)
   {::file (file (str (io/resource-path) path))}])

(defn- open-file [path]
  (clojure.java.io/input-stream (io/get-resource home-html-path)))

(defn- last-modified [path]
  (.lastModified (file (str (io/resource-path) home-html-path))))

(defresource home
  :available-media-types ["text/html"]
  :exists? (fn [_] (exist-file? home-html-path))
  :handle-ok  (fn [_] (open-file home-html-path))
  :last-modified (fn [_] (last-modified home-html-path)))

(defresource get-users
  :allowed-methods [:get]
  :handle-ok (fn [_] (generate-string @users))
  :available-media-types ["application/json"])

(defresource add-user
  :malformed? empty-user-name?
  :handle-malformed "user name can't be empty"
  :method-allowed? (request-method-in :post)
  :post! add-user-action
  :handle-created (fn [_] (generate-string @users))
  :available-media-types ["application/json"])

(defroutes home-routes
  (ANY "/" request home)
  (ANY "/add-user" request add-user)
  (ANY "/users" request get-users))
