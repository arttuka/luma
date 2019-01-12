(ns luma.styles.main
  (:require [garden.def :refer [defstyles]]
            [garden.core :as garden]))

(defstyles album-list
  [:#albums
   {:display         :flex
    :flex-wrap       :wrap
    :justify-content :space-evenly
    :min-height      "1000px"}
   [:a
    {:text-decoration :none}]
   [:.album
    {:margin-bottom "40px"
     :position      :relative}
    [:.album-card
     {:width "320px"}
     [:.album-playcount
      [:.bold
       {:font-weight :bold}]]
     [:.title
      {:font-size     "24px"
       :font-weight   :bold
       :margin-bottom "5px"}]
     [:.artist
      {:font-size "16px"}]]]])

(defstyles spotify-login-button
  [:.login-button-container
   {:position    :relative
    :float       :right
    :width       "180px"
    :height      "30px"
    :perspective "1000px"
    :cursor      :pointer}
   [:.login :.logout
    {:transform-style     :preserve-3d
     :transition          "all 0.5s linear"
     :backface-visibility :hidden
     :position            :absolute
     :overflow            :hidden
     :top                 0
     :right               0
     :cursor              :pointer}]
   [:.logout
    {:transform "rotateX(180deg)"}]
   [:&:hover
    [:.login
     {:transform "rotateX(180deg)"}]
    [:.logout
     {:transform "rotateX(360deg)"}]]]
  [:.spotify-button
   {:background-color "#1db954"
    :color            :white
    :text-decoration  :none
    :font-family      "Helvetica Neue, sans-serif"
    :font-weight      :bold
    :display          :inline-block
    :height           "30px"
    :width            "180px"
    :text-align       :center
    :line-height      "30px"
    :border-radius    "15px"
    :padding-right    "6px"
    :float            :right}
   [:img
    {:height "24px"
     :float  :left
     :margin "3px"}]
   [:div
    {:display        :inline-block
     :vertical-align :middle}]

   [:&.logout
    {:background-color :black}]])

(defstyles lastfm-login-button
  [:.lastfm-button
   {:background-color "#b90000"
    :color            :white
    :text-decoration  :none
    :font-family      "Helvetica Neue, sans-serif"
    :font-weight      :bold
    :display          :inline-block
    :height           "30px"
    :width            "180px"
    :text-align       :center
    :line-height      "30px"
    :border-radius    "15px"
    :padding-right    "6px"
    :float            :right}
   [:img
    {:height "22px"
     :float  :right
     :margin "4px"}]
   [:div
    {:display        :inline-block
     :vertical-align :middle}]

   [:&.logout
    {:background-color :black}]])

(defstyles toolbar
  [:#toolbar
   {:padding       "10px"
    :margin-bottom "20px"}
   [:.progress-bar-container
    {:float  :left
     :width  "256px"
     :height "32px"}]
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
     :min-height "32px"}]]
  spotify-login-button
  lastfm-login-button)

(defstyles terms-of-use
  [:.terms-of-use
   {:font-size  "20px"
    :text-align :center
    :color      "#666666"}
   [:a
    {:text-decoration :none
     :color           :black}]
   [:img
    {:height         "1em"
     :vertical-align :baseline}]
   [:.terms-of-use-dialog
    [:close-button
     {:position :absolute
      :top      "10px"
      :right    "10px"}]]])

(defstyles header
  [:#header
   {:padding   "20px"
    :font-size "24px"}])

(defstyles screen
  [:body {:font-family "Roboto, sans-serif"
          :margin      0}]
  [:* {:box-sizing :border-box}]
  album-list
  toolbar
  header
  terms-of-use)

(def css (garden/css screen))
