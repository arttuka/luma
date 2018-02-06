(ns luma.views
  (:require [re-frame.core :as re-frame]
            [goog.string :as gstring]
            [luma.subs :as subs]))

(defn spotify-login []
  (let [client-id "97cb16cd72dc4614b2f9a097a92d0d5c"
        state "luma"
        redirect-uri "http://localhost:8080/spotify-callback"
        response-type "code"
        scopes "user-library-read"]
    [:a {:href (gstring/format "https://accounts.spotify.com/authorize?client_id=%s&response_type=%s&state=%s&scope=%s&redirect_uri=%s"
                               client-id response-type state scopes redirect-uri)}
     "Login with Spotify"]))

;; home

(defn home-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div (str "Hello from " @name ". This is the Home Page.")
     [:div [spotify-login]]]))


;; main

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [show-panel @active-panel]))
