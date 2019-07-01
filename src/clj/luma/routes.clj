(ns luma.routes
  (:require [config.core :refer [env]]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [resources not-found]]
            [clojure.java.io :as io]
            [cheshire.core :as json]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [ring.util.response :refer [content-type header redirect response]]
            [hiccup.page :refer [include-js include-css html5]]
            [luma.integration.spotify :as spotify]
            [luma.integration.lastfm :as lastfm]
            [luma.websocket :as websocket])
  (:import (java.util UUID)))

(def asset-manifest (delay (some-> (io/resource "manifest.json")
                                   (io/reader)
                                   (json/parse-stream))))

(defn index []
  (let [html (html5
              {:lang :en}
              [:head
               [:title "LUMA Ultimate Music Archive"]
               [:meta {:name    "viewport"
                       :content "width=device-width, initial-scale=1"}]
               (include-css (if (env :dev)
                              "/css/screen.css"
                              (@asset-manifest "css/screen.min.css")))]
              [:body
               [:div#app]
               [:script (str "var csrf_token = '" *anti-forgery-token* "';")]
               (include-js (if (env :dev)
                             "/js/dev-main.js"
                             (@asset-manifest "js/prod-main.js")))
               [:script "luma.core.init();"]])]
    (-> (response html)
        (content-type "text/html")
        (header "Cache-Control" "no-cache"))))

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
