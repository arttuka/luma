(ns luma.view
  (:require [reagent-material-ui.components :as ui]
            [reagent-material-ui.icons.sort-by-alpha :refer [sort-by-alpha]]
            [reagent-material-ui.icons.cancel :refer [cancel]]
            [reagent-material-ui.styles :as styles]
            [clojure.string :as str]
            [reagent.core :as reagent :refer [atom]]
            [re-frame.core :as re-frame]
            [goog.string :as gstring]
            [oops.core :refer [oget]]
            [luma.components.album :refer [albums]]
            [luma.components.toolbar :refer [toolbar]]
            [luma.events :as events]
            [luma.subs :as subs]
            [luma.util :refer [debounce wrap-on-change]]
            [luma.websocket :as ws]))

(defn welcome-screen []
  [ui/paper {:style {:max-width  "600px"
                     :padding    "10px"
                     :margin-top "16px"
                     :height     "100%"}}
   [:h2 "LUMA Ultimate Music Archive"]
   [:p "Welcome to LUMA, a music archive that helps you sort your Spotify Music Library."]
   [:p "To begin, login with your Spotify account. Loading the tags for your albums will take some time on the first login."]
   [:p "You can also login with your Last.fm account to see play counts for the albums. Loading the playcounts will take a lot of time on the first login."]
   [:p "By logging in, you allow LUMA to use and process data about your Spotify and Last.fm accounts. For more information, see terms of use."]])

(defn erase-lastfm-data []
  (let [confirm (atom false)
        done (atom false)
        lastfm-id (re-frame/subscribe [::subs/lastfm-id])]
    (fn erase-last-fm-data-render []
      [:div.erase-data
       {:class    (cond
                    @confirm :confirm
                    (not @lastfm-id) :disabled)
        :on-click (fn []
                    (when @lastfm-id
                      (if @confirm
                        (do
                          (re-frame/dispatch [::ws/send [::events/erase-lastfm-data]])
                          (reset! done true)
                          (js/setTimeout #(set! (.-location js/window) "/logout") 3000))
                        (do
                          (reset! confirm true)
                          (js/setTimeout #(reset! confirm false) 5000)))))}
       [:div.button.front
        {:class (when @done :done)}
        (cond
          @done "Last.fm data erased!"
          @lastfm-id "Erase my Last.fm data"
          :else "Not logged in with Last.fm")]
       [:div.button.back
        "Really erase my Last.fm data"]])))

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
       [ui/dialog {:class-name :terms-of-use-dialog
                   :open       @dialog-open
                   :on-close   #(reset! dialog-open false)}
        [ui/dialog-title
         "Terms of Use"]
        [ui/dialog-content {:dividers true}
         [:p "Terms of use of LUMA Ultimate Music Archive (\"service\") as required by European Union General Data Protection Regulation (EU 2016/679) and Finnish Personal Data Act (FI 523/1999)"]

         [:h4 "Controller of data"]
         [:p
          "LUMA Ultimate Music Archive, representative Arttu Kaipiainen ("
          [:a {:href "mailto:admin@luma.dy.fi"}
           "admin@luma.dy.fi"]
          ")"]
         [:h4 "Purpose of processing personal data"]
         [:p
          "Augmenting and displaying data from user's Spotify music library."]
         [:h4 "Personal data processed"]
         [:p
          "User's Spotify ID and saved albums in their Spotify music library. User's Last.fm ID and playcount data."]
         [:h4 "Storage of personal data"]
         [:p
          "Personal data from Spotify is not stored anywhere except the user's web browser and browsing session."
          "Personal data from Last.fm is stored in the service."]
         [:h4 "Consent to process personal data"]
         [:p
          "The user gives their consent to process any personal data from their Spotify account by logging into the service with their Spotify account.
           The user may withdraw this consent at any time by logging out of the service."]
         [:p
          "The user gives their consent to process any personal data from their Last.fm account by logging into the service with their Last.fm account.
           The user may withdraw this consent at any time by using the button under the heading \"Right to be forgotten\"."]
         [:h4 "Right to obtain personal data"]
         [:p
          "All personal data being processed is visible on the front page of the service. No other personal data is stored by the service."]
         [:h4 "Right to be forgotten"]
         [:p
          "Personal data from Spotify is erased when the user logs out or otherwise stops using the service.
           Personal data from Last.fm can be erased using this button:"]
         [erase-lastfm-data]
         [:h4 "Processing of sensitive personal data"]
         [:p
          "The service doesn't process any sensitive personal data."]]
        [ui/dialog-actions
         [ui/button {:color    :primary
                     :on-click #(reset! dialog-open false)}
          "Close"]]]])))

(defn header []
  [:div#header {:style {:color :white}}
   "LUMA Ultimate Music Archive"])

(defn snackbar []
  (let [error (re-frame/subscribe [::subs/error])]
    (fn snackbar-render []
      [ui/snackbar {:open                (boolean @error)
                    :on-request-close    #(re-frame/dispatch [::events/close-error])
                    :message             (or (:msg @error) "")
                    :action              (when (:retry-event @error) "retry")
                    :on-action-touch-tap #(do (re-frame/dispatch [::events/close-error])
                                              (re-frame/dispatch [::ws/send [(:retry-event @error)]]))}])))

(defn main-panel []
  [styles/theme-provider (styles/create-mui-theme {})
   [:div
    [header]
    [toolbar]
    [:div#content
     [albums]]
    [terms-of-use]
    [snackbar]]])
