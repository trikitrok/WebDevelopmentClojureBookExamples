(ns picture-gallery.routes.home
  (:require
    [compojure.core :refer :all]
    [picture-gallery.views.layout :as layout]
    [picture-gallery.routes.gallery :refer [show-galleries]]))

(defn home []
  (layout/common
    (show-galleries)))

(defroutes home-routes
  (GET "/" [] (home)))
