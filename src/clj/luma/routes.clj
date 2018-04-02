(ns luma.routes
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [resources]]
            [ring.util.response :refer [resource-response content-type redirect]]
            [luma.integration.spotify :as spotify]
            [luma.websocket :as websocket])
  (:import (java.util UUID)))

(defroutes routes
  (GET "/" req
    (let [session (:session req)
          response (->
                     (resource-response "index.html" {:root "public"})
                     (content-type "text/html"))
          uid (or (:uid session) (UUID/randomUUID))]
      (assoc response :session (assoc session :uid uid))))
  (GET "/logout" []
    (->
      (redirect "/")
      (assoc :session {})))
  spotify/routes
  websocket/routes
  (resources "/"))
