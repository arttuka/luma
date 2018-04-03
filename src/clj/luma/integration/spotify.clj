(ns luma.integration.spotify
  (:require [compojure.core :refer [GET defroutes]]
            [ring.util.response :refer [response redirect]]
            [cheshire.core :as json]
            [org.httpkit.client :as http]
            [config.core :refer [env]]
            [luma.integration.oauth2 :as oauth2]
            [clj-time.core :as time]
            [clj-time.format :as time-fmt])
  (:import (java.util UUID)))

(defn ^:private refresh [refresh-token]
  (let [grant-type "refresh_token"
        response @(http/post "https://accounts.spotify.com/api/token"
                             {:form-params {:grant_type    grant-type
                                            :refresh_token refresh-token
                                            :client_id     (env :spotify-client-id)
                                            :client_secret (env :spotify-client-secret)}})
        body (json/parse-string (:body response) true)]
    (-> body
      (assoc :expiration (time/plus (time/now) (time/seconds (:expires_in body))))
      (select-keys [:access_token :expiration]))))

(defn ^:private get-access-token [code]
  (let [grant-type "authorization_code"
        redirect-uri (str (env :baseurl) "/spotify-callback")
        response @(http/post "https://accounts.spotify.com/api/token"
                             {:form-params {:code          code
                                            :grant_type    grant-type
                                            :redirect_uri  redirect-uri
                                            :client_id     (env :spotify-client-id)
                                            :client_secret (env :spotify-client-secret)}})
        body (json/parse-string (:body response) true)]
    (-> body
      (assoc :expiration (time/plus (time/now) (time/seconds (:expires_in body))))
      (select-keys [:access_token :refresh_token :expiration]))))

(defn ^:private get-user-info [access-token]
  (oauth2/http-get "https://api.spotify.com/v1/me" access-token))

(defn ^:private get-all-pages [url access-token]
  (loop [data []
         url url]
    (if-not url
      data
      (let [{:keys [items next]} (oauth2/http-get url access-token)]
        (recur (concat data items) next)))))

(defn get-user-albums [access-token]
  (for [a (get-all-pages "https://api.spotify.com/v1/me/albums" access-token)
        :let [album (:album a)]]
    {:id      (:id album)
     :uri     (:uri album)
     :title   (:name album)
     :image   (:url (first (:images album)))
     :added   (time-fmt/parse (time-fmt/formatters :date-time-no-ms) (:added_at a))
     :artists (for [artist (:artists album)]
                {:id   (:id artist)
                 :name (:name artist)})}))

(defn wrap-refresh-spotify [handler]
  (fn [request]
    (let [spotify-user (get-in request [:session :spotify-user])]
      (if (and spotify-user (time/before? (:expiration spotify-user) (time/now)))
        (let [new-token (refresh (:refresh_token spotify-user))
              new-session (update (:session request) :spotify-user merge new-token)
              response (handler (assoc request :session new-session))]
          (assoc response :session (merge new-session (:session response))))
        (handler request)))))

(defroutes routes
  (GET "/spotify-callback" [state code :as req]
    (if (not= (UUID/fromString state) (get-in req [:session :uid] ::not-found))
      {:status 400, :body "UID mismatch", :headers {"Content-Type" "text/plain"}}
      (let [token (get-access-token code)
            user-info (get-user-info (:access_token token))
            session (assoc (:session req) :spotify-user (assoc token :id (:id user-info)))]
        (->
          (redirect "/")
          (assoc :session session))))))
