(ns luma.app
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :as re-frame]
            [reagent-material-ui.colors :as colors]
            [reagent-material-ui.core.app-bar :refer [app-bar]]
            [reagent-material-ui.core.css-baseline :refer [css-baseline]]
            [reagent-material-ui.core.toolbar :refer [toolbar] :rename {toolbar mui-toolbar}]
            [reagent-material-ui.core.typography :refer [typography]]
            [reagent-material-ui.styles :as styles :refer [with-styles]]
            [luma.components.album :refer [albums]]
            [luma.components.snackbar :refer [snackbar]]
            [luma.components.terms-of-use :refer [terms-of-use]]
            [luma.components.toolbar :refer [toolbar]]
            [luma.subs :as subs]))

(defn styles [{:keys [spacing]}]
  {:app       {:min-height     "100vh"
               :display        :flex
               :flex-direction :column}
   :separator {:flex 1000}
   :welcome   {:align-self :center
               :max-width  640
               :padding    (spacing 2)}})

(defn welcome-screen [class]
  [:div {:class class}
   [typography {:variant       :h5
                :gutter-bottom true}
    "LUMA Ultimate Music Archive"]
   [typography {:variant   :body1
                :paragraph true}
    "Welcome to LUMA, a music archive that helps you sort your Spotify Music Library."]
   [typography {:variant   :body1
                :paragraph true}
    "To begin, login with your Spotify account. Loading the tags for your albums will take some time on the first login."]
   [typography {:variant   :body1
                :paragraph true}
    "You can also login with your Last.fm account to see play counts for the albums. Loading the playcounts will take a lot of time on the first login."]
   [typography {:variant   :body1
                :paragraph true}
    "By logging in, you allow LUMA to use and process data about your Spotify and Last.fm accounts. For more information, see terms of use."]])

(defn header []
  [app-bar {:position :static}
   [mui-toolbar
    [typography {:variant :h5}
     "LUMA Ultimate Music Archive"]]])

(def theme (styles/create-mui-theme
            {:palette {:primary   colors/blue
                       :secondary {:main (:A700 colors/red)}
                       :spotify   "#1db954"}}))

(defn app* [props]
  (let [spotify-id (re-frame/subscribe [::subs/spotify-id])]
    (fn [{:keys [classes]}]
      [styles/theme-provider theme
       [css-baseline]
       [:div {:class (:app classes)}
        [header]
        [toolbar]
        (if @spotify-id
          [albums]
          [welcome-screen (:welcome classes)])
        [:div {:class (:separator classes)}]
        [terms-of-use]
        [snackbar]]])))

(def app ((with-styles styles) app*))
