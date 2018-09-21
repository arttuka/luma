(ns luma.integration.lastfm
  (:require [config.core :refer [env]]
            [clojure.string :as str]
            [org.httpkit.client :as http]
            [cheshire.core :as json]))

(defn get-album-tags [artist album]
  (let [response @(http/get "http://ws.audioscrobbler.com/2.0/" {:query-params {:method  "album.gettoptags"
                                                                                :artist  artist
                                                                                :album   album
                                                                                :user    (env :lastfm-user)
                                                                                :api_key (env :lastfm-api-key)
                                                                                :format  "json"}
                                                                 :as           :text})]
    (if (= 200 (:status response))
      (let [json (json/parse-string (:body response) true)]
        (->>
          (:tag (:toptags json))
          (take 5)
          (map (comp str/lower-case :name))))
      (throw (ex-info "HTTP error" response)))))

(defn get-artist-tags [artist]
  (let [response @(http/get "http://ws.audioscrobbler.com/2.0/" {:query-params {:method  "artist.gettoptags"
                                                                                :artist  artist
                                                                                :user    (env :lastfm-user)
                                                                                :api_key (env :lastfm-api-key)
                                                                                :format  "json"}
                                                                 :as           :text})]
    (if (= 200 (:status response))
      (let [json (json/parse-string (:body response) true)]
        (->>
          (:tag (:toptags json))
          (take 5)
          (map (comp str/lower-case :name))))
      (throw (ex-info "HTTP error" response)))))
