(ns picture-gallery.routes.upload
  (:require
    [compojure.core :refer [defroutes GET POST]]
    [hiccup.form :refer :all]
    [hiccup.element :refer [image]]
    [hiccup.util :refer [url-encode]]
    [picture-gallery.views.layout :as layout]
    [noir.io :refer [upload-file resource-path]]
    [noir.session :as session]
    [noir.response :as resp]
    [noir.util.route :refer [restricted]]
    [clojure.java.io :as io]
    [ring.util.response :refer [file-response]]
    [picture-gallery.models.db :as db])
  (:import
    [java.io File FileInputStream FileOutputStream]
    [java.awt.image AffineTransformOp BufferedImage]
    java.awt.RenderingHints
    java.awt.geom.AffineTransform
    javax.imageio.ImageIO))

(declare upload-page
         handle-upload
         gallery-path
         serve-file
         save-thumbnail
         thumb-prefix)

(defroutes
  upload-routes

  (GET "/upload" [info]
    (upload-page info))

  (POST "/upload" [file]
    (handle-upload file))

  (GET "/img/:file-name" [file-name]
    (serve-file file-name)))

(defn upload-page [info]
  (layout/common
    [:h2 "Upload an image"]
    [:p info]
    (form-to
      {:enctype "multipart/form-data"}
      [:post "/upload"]
      (file-upload :file)
      (submit-button "upload"))))

(defn handle-upload [{:keys [filename] :as file}]
  (upload-page
    (if (empty? filename)
      "please select a file to upload"
      (try
        (upload-file (gallery-path) file :create-path? true)
        (save-thumbnail file)
        (image {:height "150px"}
               (str "/img/" thumb-prefix (url-encode filename)))
        (catch Exception ex
          (str "error uploading file " (.getMessage ex)))))))

(defn gallery-path []
  "galleries")

(defn serve-file [file-name]
  (file-response (str (gallery-path) File/separator file-name)))

(def thumb-size 150)
(def thumb-prefix "thumb_")

(defn scale [img ratio width height]
  (let [scale (AffineTransform/getScaleInstance
                (double ratio) (double ratio))
        transform-op (AffineTransformOp.
                       scale
                       AffineTransformOp/TYPE_BILINEAR)]
    (.filter transform-op
             img
             (BufferedImage. width height (.getType img)))))

(defn scale-image [file]
  (let [img (ImageIO/read file)
        img-width (.getWidth img)
        img-height (.getHeight img)
        ratio (/ thumb-size img-height)]
    (scale img ratio (int (* img-width ratio)) thumb-size)))

(defn save-thumbnail [{:keys [filename]}]
  (let [path (str (gallery-path) File/separator)]
    (ImageIO/write
      (scale-image (io/input-stream (str path filename)))
      "jpeg"
      (File. (str path thumb-prefix filename)))))
