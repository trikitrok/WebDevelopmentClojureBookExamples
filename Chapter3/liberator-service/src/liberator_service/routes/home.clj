(ns liberator-service.routes.home
  (:require [compojure.core :refer :all]
            [liberator.core
             :refer [defresource]]))

(defn- request-method [context]
  (get-in context [:request :request-method]))

(defresource home
  :method-allowed?
  (fn [context]
    (= :get (request-method context)))
  :handle-ok "Hello World!"
  :etag "fixed-etag"
  :available-media-types ["text/plain"])

(defroutes home-routes
  (ANY "/" request home))
