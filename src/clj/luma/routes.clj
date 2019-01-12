(ns luma.routes
  (:require [config.core :refer [env]]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [resources not-found]]
            [ring.util.response :refer [response content-type redirect]]
            [hiccup.page :refer [include-js include-css html5]]
            [luma.integration.spotify :as spotify]
            [luma.integration.lastfm :as lastfm]
            [luma.websocket :as websocket])
  (:import (java.util UUID)))

(let [html (delay (html5
                   {:lang :en}
                   [:head
                    [:title "LUMA Ultimate Music Archive"]
                    [:meta {:name    "viewport"
                            :content "width=device-width, initial-scale=1"}]
                    (include-css "https://fonts.googleapis.com/css?family=Roboto:300,400,500"
                                 (if (env :dev)
                                   "/css/screen.css"
                                   "/css/screen.min.css"))]
                   [:body
                    [:div#app]
                    (include-js (if (env :dev)
                                  "/js/dev-main.js"
                                  "/js/prod-main.js"))
                    [:script "luma.core.init();"]]))]
  (defn index []
    (-> (response @html)
        (content-type "text/html"))))

(defroutes routes
  (GET "/" req
    (let [session (:session req)
          response (index)
          uid (or (:uid session) (UUID/randomUUID))]
      (assoc response :session (assoc session :uid uid))))
  (GET "/logout" []
    (->
     (redirect "/")
     (assoc :session {})))
  spotify/routes
  lastfm/routes
  websocket/routes
  (resources "/")
  (not-found "Not Found"))
