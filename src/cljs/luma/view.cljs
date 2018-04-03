(ns luma.view
  (:require [cljsjs.material-ui]
            [cljs-react-material-ui.core :refer [get-mui-theme]]
            [cljs-react-material-ui.reagent :as ui]
            [cljs-react-material-ui.icons :as icons]
            [reagent.core :as reagent :refer [atom]]
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
  (let [data (re-frame/subscribe [::subs/sorted-albums])
        spotify-id (re-frame/subscribe [::subs/spotify-id])]
    (fn albums-render []
      [:div#albums
       (if @spotify-id
         (if (seq @data)
           (for [a @data]
             ^{:key (:id a)}
             [album a])
           [ui/circular-progress {:style     {:margin-top "20px"}
                                  :size      100
                                  :thickness 5}])
         [ui/paper {:class-name :intro
                    :style      {:width   "600px"
                                 :padding "10px"
                                 :height  "220px"}}
          [:h2 "LUMA Ultimate Music Archive"]
          [:p "Welcome to LUMA, a music archive that helps you sort your Spotify Music Library."]
          [:p "To begin, login with your Spotify account."]
          [:p "By logging in, you allow LUMA to use and process data about your Spotify account. For more information, see terms of use."]])])))

(defn terms-of-use []
  (let [dialog-open (atom false)]
    (fn terms-of-use-render []
      [:div.terms-of-use
       [:hr]
       [:p
        [:a {:href "#" :on-click #(reset! dialog-open true)}
         "View terms of use."]
        [:br]
        "Data from "
        [:a.spotify {:href "https://www.spotify.com"}
         [:img {:src "/images/Spotify_Logo_RGB_Black.png"
                :alt "Spotify"}]]
        " used with permission."
        [:br]
        "Data from "
        [:a.lastfm {:href "https://www.last.fm"}
         [:img {:src "/images/Last.fm_Logo_Black.png"
                :alt "Last.fm"}]]
        " used with permission."
        [:br]
        [:a.github {:href "https://github.com/arttuka/luma"}
         "View source on "
         [:img {:src "/images/GitHub-Mark-32px.png"
                :alt "GitHub logo"}]
         [:img {:src "/images/GitHub_Logo.png"
                :alt "GitHub"}]
         "."]]
       [ui/dialog {:class-name               :terms-of-use-dialog
                   :title                    "Terms of Use"
                   :modal                    false
                   :open                     @dialog-open
                   :on-request-close         #(reset! dialog-open false)
                   :auto-scroll-body-content true
                   :actions                  [(reagent/as-element [ui/flat-button {:label    "Close"
                                                                                   :primary  true
                                                                                   :on-click #(reset! dialog-open false)}])]}
        [:p "Terms of use of LUMA Ultimate Music Archive (\"service\") as required by European Union General Data Protection Regulation (EU 2016/679) and Finnish Personal Data Act (FI 523/1999)"]

        [:h4 "Controller of data"]
        [:p
         "LUMA Ultimate Music Archive, representative Arttu Kaipiainen "
         [:a {:href "mailto:admin@luma.dy.fi"}
          "admin@luma.dy.fi"]]
        [:h4 "Purpose of processing personal data"]
        [:p
         "Augmenting and displaying data from user's Spotify music library."]
        [:h4 "Personal data processed"]
        [:p
         "User's Spotify ID and saved albums in their Spotify music library."]
        [:h4 "Storage of personal data"]
        [:p
         "No personal data is stored anywhere except the user's web browser and browsing session. Any personal data is erased when user logs out or otherwise stops using the service."]
        [:h4 "Consent to process personal data"]
        [:p
         "The user gives their consent to process any personal data from their Spotify account by logging into the service with their Spotify account.
          The user may withdraw this consent at any time by logging out of the service."]
        [:h4 "Right to obtain personal data"]
        [:p
         "All personal data being processed is visible on the front page of the service. No personal data is stored otherwise by the service."]
        [:h4 "Right to be forgotten"]
        [:p
         "Any personal data is erased when user logs out or otherwise stops using the service."]
        [:h4 "Processing of sensitive personal data"]
        [:p
         "The service doesn't process any sensitive personal data."]]])))

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
    [albums]
    [terms-of-use]]])
