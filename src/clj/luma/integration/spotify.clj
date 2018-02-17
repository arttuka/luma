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
  (:import (java.util UUID)
           (clojure.lang ExceptionInfo)))

(defn ^:private refresh [refresh-token]
  (let [grant-type "refresh_token"
        response @(http/post "https://accounts.spotify.com/api/token"
                             {:form-params {:grant_type    grant-type
                                            :refresh_token refresh-token
                                            :client_id     (env :client-id)
                                            :client_secret (env :client-secret)}})
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
                                            :client_id     (env :client-id)
                                            :client_secret (env :client-secret)}})
        body (json/parse-string (:body response) true)]
    (-> body
      (assoc :expiration (time/plus (time/now) (time/seconds (:expires_in body))))
      (select-keys [:access_token :refresh_token :expiration]))))

(defn ^:private refreshing
  ([method url user-id access-token refresh-token]
   (refreshing method url {} user-id access-token refresh-token))
  ([method url options user-id access-token refresh-token]
   (let [http-fn (case method
                   :get oauth2/http-get)]
     (try
       (http-fn url access-token options)
       (catch ExceptionInfo ex
         (if (and (= "HTTP error" (.getMessage ex))
                  (= 401 (:status (ex-data ex))))
           (let [token (refresh refresh-token)]
             (db/save-account user-id token)
             (http-fn url (:access_token token) options))
           (throw ex)))))))

(defn ^:private get-user-info [access-token]
  (oauth2/http-get "https://api.spotify.com/v1/me" access-token))

(defn ^:private get-all-pages [url user-id access-token refresh-token]
  (loop [data []
         url url]
    (if-not url
      data
      (let [{:keys [items next]} (refreshing :get url user-id access-token refresh-token)]
        (recur (concat data items) next)))))

(defn get-user-albums [user-id access-token refresh-token]
  (for [a (get-all-pages "https://api.spotify.com/v1/me/albums" user-id access-token refresh-token)
        :let [album (:album a)]]
    {:id      (:id album)
     :uri     (:uri album)
     :title   (:name album)
     :image   (:url (first (:images album)))
     :artists (for [artist (:artists album)]
                {:id   (:id artist)
                 :name (:name artist)})}))

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
