(ns luma.styles.main
  (:require [garden.def :refer [defstyles]]
            [garden.core :as garden]
            [garden.stylesheet :refer [at-import]]
            [luma.util :refer [when-desktop when-mobile]]))

(defstyles album-list
  [:#albums
   {:display         :flex
    :flex-wrap       :wrap
    :justify-content :space-around
    :min-height      "1000px"
    :padding         "8px 16px"}
   [:.album-card
    {:position :relative
     :margin   "8px"}
    [:.album-playcount
     [:.bold
      {:font-weight :bold}]]
    [:.title
     {:font-size     "24px"
      :font-weight   :bold
      :margin-bottom "5px"}]
    [:.artist
     {:font-size "16px"}]]
   (when-mobile
    (let [width "calc(100vw - 32px)"]
      [:.album-card
       {:width width}
       [:.album-image
        {:width  width
         :height width}]]))
   (when-desktop
    (let [width "320px"]
      [:.album-card
       {:width width}
       [:.album-image
        {:width  width
         :height width}]]))])

(defstyles login-button
  [:.login-button-container
   {:position    :relative
    :float       :right
    :width       "200px"
    :height      "30px"
    :perspective "1000px"
    :cursor      :pointer}
   [:.login-button
    {:color                       :white
     :font-family                 "Helvetica Neue, sans-serif"
     :display                     :inline-block
     :font-weight                 :bold
     :white-space                 :nowrap
     :height                      "30px"
     :line-height                 "30px"
     :min-width                   "200px"
     :border-radius               "15px"
     :width                       :fit-content
     :text-align                  :center
     :float                       :right
     :transform-style             :preserve-3d
     :transition                  "all 0.5s linear"
     :backface-visibility         :hidden
     :-webkit-backface-visibility :hidden
     :position                    :absolute
     :overflow                    :hidden
     :top                         0
     :right                       0
     :cursor                      :pointer}
    [:img
     {:position :absolute}]
    [:&.logout
     {:background-color :black
      :transform        "rotateX(180deg)"}]]
   [:&:hover
    [:.login
     {:transform "rotateX(180deg)"}]
    [:.logout
     {:transform "rotateX(360deg)"}]]])

(defstyles spotify-login-button
  [:.spotify
   {:margin-left   "16px"
    :margin-bottom "16px"}
   [:.login-button
    {:background-color "#1db954"
     :padding-right    "6px"
     :padding-left     "33px"}
    [:img
     {:height "24px"
      :left   "3px"
      :top    "3px"}]]])

(defstyles lastfm-login-button
  [:.lastfm
   [:.login-button
    {:background-color "#b90000"
     :padding-left     "86px"
     :padding-right    "6px"}
    [:img
     {:height "22px"
      :left   "6px"
      :top    "4px"}]]])

(defstyles toolbar
  [:#toolbar
   {:padding "16px 16px 0"}
   [:.progress-bar-container
    {:float         :left
     :width         "256px"
     :height        "32px"
     :margin-bottom "16px"}]
   [:.tag-filter
    {:float        :left
     :width        "256px"
     :margin-right "12px"}]
   [:.sort-container
    {:float    :left
     :position :relative}]
   [:.selected-tags
    {:float      :left
     :clear      :left
     :display    :flex
     :flex-wrap  :wrap
     :min-height "48px"}]
   login-button
   spotify-login-button
   lastfm-login-button])

(defstyles terms-of-use
  [:.terms-of-use
   {:font-size  "20px"
    :text-align :center
    :color      "#666666"}
   [:a
    {:color :black}]
   [:img
    {:height         "1em"
     :vertical-align :baseline}]])

(defstyles header
  [:#header
   {:padding   "20px"
    :font-size "24px"}])

(defstyles screen
  (at-import "https://fonts.googleapis.com/css?family=Roboto:300,400,500")
  [:body {:font-family "Roboto, sans-serif"
          :margin      0}]
  [:* {:box-sizing :border-box}]
  [:a {:text-decoration :none}]
  album-list
  toolbar
  header
  terms-of-use)

(def css (garden/css screen))
