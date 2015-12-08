(ns picture-gallery.models.db
  (:require [clojure.java.jdbc :as sql]))

(def db
  {:subprotocol "postgresql"
   :subname     "//localhost/gallery"
   :user        "admin"
   :password    "admin"})

(defn create-user [user]
  (sql/with-connection
    db
    (sql/insert-record :users user)))

(defn get-user [id]
  (sql/with-connection
    db
    (sql/with-query-results
      res
      ["select * from users where id = ?" id]
      (first res))))
