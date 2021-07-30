(ns luma.components.terms-of-use
  (:require [reagent.core :as reagent :refer [atom with-let]]
            [re-frame.core :as re-frame]
            [reagent-material-ui.core.button :refer [button]]
            [reagent-material-ui.core.dialog :refer [dialog]]
            [reagent-material-ui.core.dialog-actions :refer [dialog-actions]]
            [reagent-material-ui.core.dialog-content :refer [dialog-content]]
            [reagent-material-ui.core.dialog-title :refer [dialog-title]]
            [reagent-material-ui.core.divider :refer [divider]]
            [reagent-material-ui.core.link :refer [link]]
            [reagent-material-ui.core.typography :refer [typography]]
            [reagent-material-ui.styles :refer [styled]]
            [luma.events :as events]
            [luma.subs :as subs]
            [luma.websocket :as ws]))

(defn erase-lastfm-data []
  (with-let [confirm (atom false)
             done (atom false)
             lastfm-id (re-frame/subscribe [::subs/lastfm-id])
             on-click (fn []
                        (cond
                          (or @done (not @lastfm-id)) :no-op
                          @confirm (do
                                     (ws/send! [::events/erase-lastfm-data])
                                     (reset! done true)
                                     (js/setTimeout #(set! (.-location js/window) "/logout") 3000))
                          :else (do
                                  (reset! confirm true)
                                  (js/setTimeout #(reset! confirm false) 5000))))]
    [button {:sx       {:width 260}
             :variant  :contained
             :disabled (not @lastfm-id)
             :color    (cond
                         @done :primary
                         @confirm :secondary
                         :else :inherit)
             :on-click on-click}
     (cond
       @done "Last.fm data erased!"
       @confirm "Really erase my Last.fm data"
       @lastfm-id "Erase my Last.fm data"
       :else "Not logged in with Last.fm")]))

(defn subtitle [text]
  [typography {:variant :h6}
   text])

(def img (styled "img" {:height "1em"}))

(defn terms-of-use []
  (with-let [dialog-open? (atom false)
             open-dialog #(reset! dialog-open? true)
             close-dialog #(reset! dialog-open? false)]
    [:<>
     [divider]
     [typography {:align :center}
      [link {:on-click  open-dialog
             :href      "#"
             :underline :none}
       "View terms of use."]
      [:br]
      "Data from "
      [link {:href      "https://www.spotify.com"
             :underline :none}
       [img {:src "/images/Spotify_Logo_RGB_Black.png"
             :alt "Spotify"}]]
      " used with permission."
      [:br]
      "Data from "
      [link {:href      "https://www.last.fm"
             :underline :none}
       [img {:src "/images/Last.fm_Logo_Black.png"
             :alt "Last.fm"}]]
      " used with permission."
      [:br]
      [link {:href      "https://github.com/arttuka/luma"
             :underline :none}
       "View source on "
       [img {:src "/images/GitHub-Mark-32px.png"
             :alt "GitHub logo"}]
       [img {:src "/images/GitHub_Logo.png"
             :alt "GitHub"}]
       "."]]
     [dialog {:class-name :terms-of-use-dialog
              :open       @dialog-open?
              :on-close   close-dialog}
      [dialog-title
       "Terms of Use"]
      [dialog-content {:dividers true}
       [:p "Terms of use of LUMA Ultimate Music Archive (\"service\") as required by European Union General Data Protection Regulation (EU 2016/679) and Finnish Personal Data Act (FI 523/1999)"]

       [subtitle "Controller of data"]
       [:p
        "LUMA Ultimate Music Archive, representative Arttu Kaipiainen ("
        [link {:href "mailto:admin@luma.dy.fi"}
         "admin@luma.dy.fi"]
        ")"]
       [subtitle "Purpose of processing personal data"]
       [:p
        "Augmenting and displaying data from user's Spotify music library."]
       [subtitle "Personal data processed"]
       [:p
        "User's Spotify ID and saved albums in their Spotify music library. User's Last.fm ID and playcount data."]
       [subtitle "Storage of personal data"]
       [:p
        "Personal data from Spotify is not stored anywhere except the user's web browser and browsing session."
        "Personal data from Last.fm is stored in the service."]
       [subtitle "Consent to process personal data"]
       [:p
        "The user gives their consent to process any personal data from their Spotify account by logging into the service with their Spotify account.
         The user may withdraw this consent at any time by logging out of the service."]
       [:p
        "The user gives their consent to process any personal data from their Last.fm account by logging into the service with their Last.fm account.
         The user may withdraw this consent at any time by using the button under the heading \"Right to be forgotten\"."]
       [subtitle "Right to obtain personal data"]
       [:p
        "All personal data being processed is visible on the front page of the service. No other personal data is stored by the service."]
       [subtitle "Right to be forgotten"]
       [:p
        "Personal data from Spotify is erased when the user logs out or otherwise stops using the service.
         Personal data from Last.fm can be erased using this button:"]
       [erase-lastfm-data]
       [subtitle "Processing of sensitive personal data"]
       [:p
        "The service doesn't process any sensitive personal data."]]
      [dialog-actions
       [button {:color    :primary
                :on-click close-dialog}
        "Close"]]]]))
