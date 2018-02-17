(ns luma.views
  (:require [re-frame.core :as re-frame]
            [goog.string :as gstring]
            [luma.subs :as subs]))

(defn spotify-login []
  (let [uid (re-frame/subscribe [::subs/uid])
        spotify-id (re-frame/subscribe [::subs/spotify-id])
        client-id "97cb16cd72dc4614b2f9a097a92d0d5c"
        redirect-uri "http://localhost:8080/spotify-callback"
        response-type "code"
        scopes "user-library-read"]
    (fn []
      (if @spotify-id
        [:span (str "Logged in as " @spotify-id)]
        [:a {:href (gstring/format "https://accounts.spotify.com/authorize?client_id=%s&response_type=%s&state=%s&scope=%s&redirect_uri=%s"
                                 client-id response-type @uid scopes redirect-uri)}
       "Login with Spotify"]))))

(defn main-panel []
  [:div [spotify-login]])
