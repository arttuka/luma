(ns luma.app
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :as re-frame]
            [reagent-material-ui.components :as ui]
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
               :max-width  620
               :padding    (spacing 2)}})

(defn welcome-screen [class]
  [:div {:class class}
   [ui/typography {:variant :h5}
    "LUMA Ultimate Music Archive"]
   [:p "Welcome to LUMA, a music archive that helps you sort your Spotify Music Library."]
   [:p "To begin, login with your Spotify account. Loading the tags for your albums will take some time on the first login."]
   [:p "You can also login with your Last.fm account to see play counts for the albums. Loading the playcounts will take a lot of time on the first login."]
   [:p "By logging in, you allow LUMA to use and process data about your Spotify and Last.fm accounts. For more information, see terms of use."]])

(defn header []
  [ui/app-bar {:position :static}
   [ui/toolbar
    [ui/typography {:variant :h5}
     "LUMA Ultimate Music Archive"]]])

(defn app* [props]
  (let [spotify-id (re-frame/subscribe [::subs/spotify-id])]
    (fn [{:keys [classes]}]
      [styles/theme-provider (styles/create-mui-theme {})
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
