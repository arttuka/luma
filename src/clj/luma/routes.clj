(ns luma.routes
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [resources]]
            [ring.util.response :refer [resource-response]]
            [luma.integration.spotify :as spotify]))

(defroutes routes
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  spotify/routes
  (resources "/"))
