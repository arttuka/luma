(ns luma.views
  (:require [cljsjs.material-ui]
            [cljs-react-material-ui.core :refer [get-mui-theme]]
            [cljs-react-material-ui.reagent :as ui]
            [cljs-react-material-ui.icons :as icons]
            [reagent.core :refer [atom]]
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
    (fn spotify-login-render []
      (if @spotify-id
        [:div.login-button-container
         [:div.login.spotify-button
          [:img {:src "/images/Spotify_Icon_RGB_White.png"}]
          @spotify-id]
         [:a.logout.spotify-button
          {:href "/logout"}
          [:img {:src "/images/Spotify_Icon_RGB_White.png"}]
          (str "Log out")]]
        [:a.spotify-button {:href (gstring/format "https://accounts.spotify.com/authorize?client_id=%s&response_type=%s&state=%s&scope=%s&redirect_uri=%s"
                                                  client-id response-type @uid scopes redirect-uri)}
         [:img {:src "/images/Spotify_Icon_RGB_White.png"}]
         "Login with Spotify"]))))

(defn progress-bar []
  (let [progress (re-frame/subscribe [::subs/progress])
        albums (re-frame/subscribe [::subs/albums])]
    (fn progress-bar-render []
      [ui/linear-progress {:style {:margin-top "6px"}
                           :mode  (if (seq @albums)
                                    :determinate
                                    :indeterminate)
                           :max   (count @albums)
                           :min   0
                           :value @progress}])))

(defn selected-tags []
  (let [selected-tags (re-frame/subscribe [::subs/selected-tags])
        all-tags (re-frame/subscribe [::subs/all-tags])]
    (fn selected-tags-render []
      [:div.selected-tags
       (if @all-tags
         (for [tag @selected-tags]
           ^{:key (str "selected-tag-" tag)}
           [ui/chip
            {:class             :selected-tag
             :style             {:margin-right "5px"}
             :on-request-delete #(re-frame/dispatch [::events/unselect-tag tag])}
            tag])
         [:div.progress-bar-container
          [:div "Loading tags..."]
          [progress-bar]])])))

(defn tag-filter []
  (let [all-tags (re-frame/subscribe [::subs/all-tags])]
    (fn tag-filter-render []
      [:div.tag-filter
       [autosuggest {:disabled             (not @all-tags)
                     :datasource           @all-tags
                     :on-change            #(re-frame/dispatch [::events/select-tag %])
                     :floating-label-text  "Filter by"
                     :floating-label-fixed true
                     :hint-text            "Tag"}]])))

(defn sort-dropdown []
  (let [sort-key (re-frame/subscribe [::subs/sort-key])
        sort-asc (re-frame/subscribe [::subs/sort-asc])
        value (atom @sort-key)
        palette (.-palette (get-mui-theme))]
    (fn sort-dropdown-render []
      [:div.sort-container
       [ui/select-field {:floating-label-text "Sort by"
                         :value               @value
                         :on-change           (fn [_ _ new-value]
                                                (reset! value new-value)
                                                (re-frame/dispatch [::events/sort-albums (keyword new-value)]))}
        [ui/menu-item {:value        :artist
                       :primary-text "Artist"}]
        [ui/menu-item {:value        :album
                       :primary-text "Album title"}]
        [ui/menu-item {:value        :added
                       :primary-text "Added at"}]]
       [ui/icon-button {:on-click #(re-frame/dispatch [::events/change-sort-dir])
                        :style    {:position :absolute
                                   :top      "24px"}}
        [icons/av-sort-by-alpha {:color (if @sort-asc
                                          (.-primary1Color palette)
                                          (.-accent1Color palette))}]]])))

(defn toolbar []
  (let [spotify-id (re-frame/subscribe [::subs/spotify-id])]
    (fn toolbar-render []
      (if @spotify-id
        [ui/paper {:id :toolbar}
         [spotify-login]
         [tag-filter]
         [sort-dropdown]
         [selected-tags]
         [:div {:style {:clear :both}}]]
        [ui/paper {:id :toolbar}
         [spotify-login]
         [:div {:style {:clear :both}}]]))))

(defn album [a]
  (let [depth (atom 1)]
    (fn album-render [a]
      [:a.album
       {:href (:uri a)}
       [ui/card {:class         :album-card
                 :on-mouse-over #(reset! depth 5)
                 :on-mouse-out  #(reset! depth 1)
                 :z-depth       @depth}
        [ui/card-media
         [:img.cover {:src (:image a)}]]
        [ui/card-title
         [:div.title (:title a)]
         [:div.artists
          (for [artist (:artists a)]
            ^{:key (str (:id a) (:artist_id artist))}
            [:div.artist (:name artist)])]]
        [ui/card-text

         [:div.tags
          (for [tag (interpose " · " (:tags a))]
            tag)]]]])))

(defn albums []
  (let [data (re-frame/subscribe [::subs/sorted-albums])]
    (fn []
      [:div#albums
       (for [a @data]
         ^{:key (:id a)}
         [album a])])))

(defn header []
  (let [palette (.-palette (get-mui-theme))]
    [:div#header {:style {:background-color (.-primary1Color palette)
                          :color            :white}}
     "LUMA Ultimate Music Archive"]))

(defn main-panel []
  [ui/mui-theme-provider
   {:mui-theme (get-mui-theme)}
   [:div
    [header]
    [toolbar]
    [albums]]])
