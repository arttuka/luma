(ns luma.routes
  (:require [config.core :refer [env]]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [resources not-found]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [cheshire.core :as json]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [ring.util.response :refer [content-type header redirect response]]
            [hiccup.page :refer [include-js include-css html5]]
            [luma.integration.spotify :as spotify]
            [luma.integration.lastfm :as lastfm]
            [luma.transit :as transit]
            [luma.websocket :as websocket])
  (:import (java.util UUID)))

(def asset-manifest (delay (some-> (io/resource "manifest.json")
                                   (io/reader)
                                   (json/parse-stream))))

(defn escape-quotes [s]
  (str/escape s {\' "\\'"}))

(defn initial-db [req uid]
  {:env        {:spotify-client-id    (env :spotify-client-id)
                :spotify-redirect-uri (str (env :baseurl) "/spotify-callback")
                :lastfm-api-key       (env :lastfm-api-key)
                :lastfm-redirect-uri  (str (env :baseurl) "/lastfm-callback")}
   :spotify-id (get-in req [:session :spotify-user :id])
   :lastfm-id  (get-in req [:session :lastfm-user :name])
   :uid        uid})

(defn index [req uid]
  (let [html (html5
              {:lang :en}
              [:head
               [:title "LUMA Ultimate Music Archive"]
               [:meta {:name    "viewport"
                       :content "width=device-width, initial-scale=1, maximum-scale=1"}]
               (include-css "https://fonts.googleapis.com/css?family=Roboto:300,400,500,700&display=swap")]
              [:body
               [:div#app]
               [:script (str "var csrfToken = '" *anti-forgery-token* "'; "
                             "var initialDb = '" (escape-quotes (transit/write (initial-db req uid))) "';")]
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
          uid (or (:uid session) (UUID/randomUUID))
          response (index req uid)]
      (assoc response :session (assoc session :uid uid))))
  (GET "/logout" []
    (assoc (redirect "/") :session {}))
  spotify/routes
  lastfm/routes
  websocket/routes
  (resources "/")
  (not-found "Not Found"))
