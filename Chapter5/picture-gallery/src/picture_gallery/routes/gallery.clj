(ns picture-gallery.routes.gallery
  (:require
    [compojure.core :refer :all]
    [hiccup.element :refer :all]
    [hiccup.page :refer :all]
    [hiccup.form :refer [check-box]]
    [picture-gallery.views.layout :as layout]
    [picture-gallery.util :refer [thumb-prefix image-uri thumb-uri]]
    [picture-gallery.models.db :as db]
    [noir.session :as session]))

(defn- thumbnail-link [{:keys [userid name]}]
  [:div.thumbnail
   [:a {:class name :href (image-uri userid name)}
    (image (thumb-uri userid name))]
   (when (= userid (session/get :user))
     (check-box name))])

(defn- gallery-link [{:keys [userid name]}]
  [:div.thumbnail
   [:a
    {:href (str "/gallery/" userid)}
    (image (thumb-uri userid name))
    userid "'s gallery"]])

(defn display-gallery [userid]
  (if-let [gallery (not-empty (map thumbnail-link (db/images-by-user userid)))]
    [:div
     [:div#error]
     gallery
     (if (= userid (session/get :user))
       [:input#delete {:type "submit" :value "delete images"}])]
    [:p "The user " userid " does not have any galleries"]))

(defn show-galleries []
  (map gallery-link (db/get-gallery-previews)))

(defroutes
  gallery-routes
  (GET "/gallery/:userid" [userid]
    (layout/common
      (include-js "/js/gallery.js")
      (display-gallery userid))))