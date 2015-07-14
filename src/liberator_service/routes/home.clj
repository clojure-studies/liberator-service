(ns liberator-service.routes.home
  (:require [compojure.core :refer :all]
            [liberator.core
             :refer [defresource resource request-method-in by-method]]
            [cheshire.core :refer [generate-string]]
            [noir.io :as io]
            [clojure.java.io :refer [file]]))

(def users (atom ["John" "Jane"]))

(defresource get-users
  :allowed-methods [:get]
  :handle-ok (fn [_] (generate-string @users))
  :available-media-types ["application/json"])

(defresource add-user
  :allowed-methods [:post]
  :post!
  (fn [context]
    (let [params (get-in context [:request :form-params])]
      (swap! users conj (get params "user"))))
  :handle-created (fn [_] (generate-string @users))
  :available-media-types ["application/json"])

(defresource users-resource
  :malformed? (by-method
                {:get nil :post (fn [context]
                    (let [params (get-in context [:request :form-params])]
                      (empty? (get params "user"))))})
  :handle-malformed "user cannot be empty!"
  :allowed-methods [:get :post]
  :post!
  (fn [context]
    (let [params (get-in context [:request :form-params])]
      (swap! users conj (get params "user"))))
  :handle-created (fn [_] (generate-string @users))
  :handle-ok (fn [_] (generate-string @users))
  :available-media-types ["application/json"])

(defresource home
  :available-media-types ["text/html"]
  :exists?
  (fn [_]
    (.exists (file (str (io/resource-path) "/home.html"))))
  :handle-ok
  (fn [_]
    (clojure.java.io/input-stream (io/get-resource "/home.html")))
  :last-modified
  (fn [_]
    (.lastModified (file (str (io/resource-path) "/home.html")))))

(defroutes home-routes
  (ANY "/" request home)
  (ANY "/users" request users-resource))
