(ns liberator-service.routes.home
  (:require [compojure.core :refer :all]
            [liberator.core
             :refer [defresource request-method-in]]))

(defn- used-request-method [context]
  (get-in context [:request :request-method]))

(defresource home
  :service-available? true
  :method-allowed? (request-method-in :get)
  :handle-method-not-allowed
  (fn [context]
    (str (used-request-method context) " is not allowed"))
  :handle-ok "Hello World!"
  :etag "fixed-etag"
  :available-media-types ["text/plain"])

(defroutes home-routes
  (ANY "/" request home))
