(ns luma.integration.lastfm
  (:require [compojure.core :refer [GET defroutes]]
            [ring.util.response :refer [redirect]]
            [config.core :refer [env]]
            [clojure.string :as str]
            [org.httpkit.client :as http]
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
        param-str (str param-str (env :lastfm-shared-secret))
        md5-str (md5 param-str)
        pad (apply str (repeat (- 32 (count md5-str)) "0"))]
    (str pad md5-str)))

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
         response @(http/get "http://ws.audioscrobbler.com/2.0/" {:query-params query-params
                                                                  :as           :text})]
     (if (= 200 (:status response))
       (json/parse-string (:body response) true)
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

(defn get-session [token]
  (:session (lastfm-request :auth.getsession {:token token} true)))

(defroutes routes
  (GET "/lastfm-callback" [token :as req]
    (let [lastfm-user (get-session token)
          session (assoc (:session req) :lastfm-user lastfm-user)]
      (-> (redirect "/")
          (assoc :session session)))))
