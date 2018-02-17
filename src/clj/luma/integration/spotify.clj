(ns luma.integration.spotify
  (:require [clojure.data.codec.base64 :as b64]
            [compojure.core :refer [GET defroutes]]
            [ring.util.response :refer [response redirect]]
            [cheshire.core :as json]
            [org.httpkit.client :as http]
            [config.core :refer [env]]
            [luma.integration.oauth2 :as oauth2]
            [clj-time.core :as time]
            [luma.db :as db])
  (:import (java.util UUID)))

(defn ^:private get-access-token [code]
  (let [grant-type "authorization_code"
        redirect-uri (str (env :baseurl) "/spotify-callback")
        response @(http/post "https://accounts.spotify.com/api/token"
                             {:form-params {:code          code
                                            :grant_type    grant-type
                                            :redirect_uri  redirect-uri
                                            :client_id     (env :client-id)
                                            :client_secret (env :client-secret)}})
        body (json/parse-string (:body response) true)]
    (-> body
      (assoc :expiration (time/plus (time/now) (time/seconds (:expires_in body))))
      (select-keys [:access_token :refresh_token :expiration]))))

(defn ^:private get-user-info [access-token]
  (oauth2/http-get "https://api.spotify.com/v1/me" access-token))

(defroutes routes
  (GET "/spotify-callback" [state code :as req]
    (if (not= (UUID/fromString state) (get-in req [:session :uid] ::not-found))
      {:status 400, :body "UID mismatch", :headers {"Content-Type" "text/plain"}}
      (let [token (get-access-token code)
            user-info (get-user-info (:access_token token))
            session (assoc (:session req) :spotify-id (:id user-info))]
        (db/save-account (:id user-info) token)
        (->
          (redirect "/")
          (assoc :session session))))))
