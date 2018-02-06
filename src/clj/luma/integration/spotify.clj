(ns luma.integration.spotify
  (:require [clojure.data.codec.base64 :as b64]
            [compojure.core :refer [GET defroutes]]
            [ring.util.response :refer [redirect]]
            [cheshire.core :as json]
            [org.httpkit.client :as http]
            [luma.config :as config]))

(defonce ^:private access-tokens (atom nil))

(defn ^:private get-access-token [code]
  (let [grant-type "authorization_code"
        redirect-uri "http://localhost:8080/spotify-callback"
        response @(http/post "https://accounts.spotify.com/api/token"
                             {:form-params {:code          code
                                            :grant_type    grant-type
                                            :redirect_uri  redirect-uri
                                            :client_id     (config/get :client-id)
                                            :client_secret (config/get :client-secret)}})
        body (json/parse-string (:body response))]
    body))

(defroutes routes
  (GET "/spotify-callback" [state code]
    (swap! access-tokens assoc state (get-access-token code))
    (redirect "/" 303)))

