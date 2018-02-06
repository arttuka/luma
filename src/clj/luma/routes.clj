(ns luma.routes
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [resources]]
            [ring.util.response :refer [resource-response content-type]]
            [luma.integration.spotify :as spotify]
            [luma.websocket :as websocket]))

(defroutes routes
  (GET "/" [] (->
                (resource-response "index.html" {:root "public"})
                (content-type "text/html")))
  spotify/routes
  websocket/routes
  (resources "/"))
