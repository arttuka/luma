(ns luma.integration.lastfm
  (:require [compojure.core :refer [GET defroutes]]
            [ring.util.response :refer [redirect]]
            [config.core :refer [env]]
            [clojure.string :as str]
            [aleph.http :as http]
            [byte-streams :as bs]
            [cheshire.core :as json]
            [luma.util :refer [->hex throttle]])
  (:import (java.security MessageDigest)))

(defn md5 [^String s]
  (-> (MessageDigest/getInstance "MD5")
      (.digest (.getBytes s "UTF-8"))
      (->hex)))

(defn signature [params]
  (let [param-str (->> (dissoc params :format)
                       (sort-by key)
                       (mapcat (juxt (comp name key) val))
                       (apply str))
        param-str (str param-str (env :lastfm-shared-secret))]
    (md5 param-str)))

(defn lastfm-request*
  ([method params]
   (lastfm-request* method params false))
  ([method params sign?]
   (let [query-params (merge {:method  (name method)
                              :api_key (env :lastfm-api-key)
                              :format  "json"}
                             params)
         query-params (if sign?
                        (assoc query-params :api_sig (signature query-params))
                        query-params)
         response @(http/get "http://ws.audioscrobbler.com/2.0/" {:query-params query-params})]
     (if (= 200 (:status response))
       (-> (:body response)
           bs/to-string
           (json/parse-string true))
       (throw (ex-info "HTTP error" response))))))

(def lastfm-request (throttle lastfm-request* 5))

(defn get-album-tags [artist album]
  (->> (lastfm-request :album.gettoptags {:artist artist
                                          :album  album})
       :toptags
       :tag
       (take 5)
       (map (comp str/lower-case :name))))

(defn get-artist-tags [artist]
  (->> (lastfm-request :artist.gettoptags {:artist artist})
       :toptags
       :tag
       (take 5)
       (map (comp str/lower-case :name))))

(defn get-album-playcount [username artist album]
  (let [response (lastfm-request :album.getinfo {:artist   artist
                                                 :album    album
                                                 :username username})
        playcount (get-in response [:album :userplaycount] 0)]
    (if (string? playcount)
      (Integer/parseInt playcount)
      playcount)))

(defn get-session [token]
  (:session (lastfm-request :auth.getsession {:token token} true)))

(defroutes routes
  (GET "/lastfm-callback" [token :as req]
    (let [lastfm-user (get-session token)
          session (assoc (:session req) :lastfm-user lastfm-user)]
      (assoc (redirect "/") :session session))))
