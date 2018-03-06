(ns luma.views
  (:require [cljsjs.material-ui]
            [cljs-react-material-ui.core :refer [get-mui-theme]]
            [cljs-react-material-ui.reagent :as ui]
            [re-frame.core :as re-frame]
            [goog.string :as gstring]
            [luma.components.autosuggest :refer [autosuggest]]
            [luma.events :as events]
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

(defn selected-tags []
  (let [selected-tags (re-frame/subscribe [::subs/selected-tags])]
    (fn []
      [:div
       (for [tag @selected-tags]
         [:div.selected-tag tag])])))

(defn tag-filter []
  (let [all-tags (re-frame/subscribe [::subs/all-tags])]
    (fn []
      (when @all-tags
        [:div
         [autosuggest {:datasource @all-tags
                       :on-change  #(re-frame/dispatch [::events/select-tag %])}]
         [selected-tags]]))))

(defn album [a]
  [:a.album
   {:href (:uri a)}
   [:img.cover {:src (:image a)}]
   [:div.title
    (:title a)]
   [:div.artists
    (for [artist (:artists a)]
      ^{:key (str (:id a) (:artist_id artist))}
      [:div.artist (:name artist)])]
   [:div.tags
    (for [tag (interpose " · " (:tags a))]
      tag)]])

(defn albums []
  (let [data (re-frame/subscribe [::subs/filtered-albums])]
    (fn []
      [:div#albums
       (for [a @data]
         ^{:key (:id a)}
         [album a])])))

(defn main-panel []
  [ui/mui-theme-provider
   {:mui-theme (get-mui-theme)}
   [:div
    [spotify-login]
    [tag-filter]
    [albums]]])
