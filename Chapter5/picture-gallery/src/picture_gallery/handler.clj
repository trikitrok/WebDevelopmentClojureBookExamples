(ns picture-gallery.handler
  (:require [compojure.route :as route]
            [compojure.core :refer [defroutes]]
            [picture-gallery.routes.home :refer [home-routes]]
            [noir.util.middleware :as noir-middleware]
            [picture-gallery.routes.auth :refer [auth-routes]]
            [picture-gallery.routes.upload :refer [upload-routes]]
            [noir.session :as session]))

(defn init []
  (println "picture-gallery is starting"))

(defn destroy []
  (println "picture-gallery is shutting down"))

(defn user-page [_]
  (session/get :user))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (noir-middleware/app-handler
    [auth-routes
     home-routes
     upload-routes
     app-routes]
    :access-rules [user-page]))
