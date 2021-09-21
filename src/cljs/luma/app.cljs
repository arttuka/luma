(ns luma.app
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :as re-frame]
            [reagent-mui.colors :as colors]
            [reagent-mui.material.app-bar :refer [app-bar]]
            [reagent-mui.material.css-baseline :refer [css-baseline]]
            [reagent-mui.material.toolbar :refer [toolbar] :rename {toolbar mui-toolbar}]
            [reagent-mui.material.typography :refer [typography]]
            [reagent-mui.styles :as styles :refer [styled]]
            [luma.components.album :refer [albums]]
            [luma.components.snackbar :refer [snackbar]]
            [luma.components.terms-of-use :refer [terms-of-use]]
            [luma.components.toolbar :refer [toolbar]]
            [luma.subs :as subs]
            [luma.util :as util]))

(def classes
  (util/make-classes "luma-app"
                     [:root
                      :separator
                      :welcome]))

(defn styles [{{:keys [spacing]} :theme}]
  (util/set-classes
   classes
   {:root      {:min-height     "100vh"
                :display        :flex
                :flex-direction :column}
    :separator {:flex 1000}
    :welcome   {:align-self :center
                :max-width  640
                :padding    (spacing 2)}}))

(defn welcome-screen []
  [:div {:class (:welcome classes)}
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

(def theme (styles/create-theme
            {:palette {:primary   colors/blue
                       :secondary {:main (:A700 colors/red)}
                       :spotify   "#1db954"}}))

(defn app* [{:keys [class-name]}]
  [styles/theme-provider theme
   [css-baseline]
   [:div {:class [class-name (:root classes)]}
    [header]
    [toolbar]
    (if @(re-frame/subscribe [::subs/spotify-id])
      [albums]
      [welcome-screen])
    [:div {:class (:separator classes)}]
    [terms-of-use]
    [snackbar]]])

(def app (styled app* styles))
