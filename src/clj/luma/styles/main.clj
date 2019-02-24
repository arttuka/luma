(ns luma.styles.main
  (:require [garden.def :refer [defstyles]]
            [garden.core :as garden]
            [garden.stylesheet :refer [at-import]]
            [luma.util :refer [when-desktop when-mobile]]))

(defn album-card [width margin]
  [:.album-card
   {:width  width
    :margin margin}
   [:.album-image
    {:width  width
     :height width}]])

(defstyles album-list
  [:#albums
   [:.album-card
    {:position :relative}
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
    (album-card "calc(100vw - 32px)" "16px"))
   (when-desktop
    (album-card "320px" "8px")
    [:&
     {:display         :flex
      :flex-wrap       :wrap
      :justify-content :center
      :padding         "8px 16px"}])])

(defn flipping [selector]
  [[:&
    {:position :relative}
    [:.front :.back
     {:display                     :block
      :transform-style             :preserve-3d
      :transition                  "all 0.5s linear"
      :backface-visibility         :hidden
      :-webkit-backface-visibility :hidden
      :position                    :absolute
      :top                         0
      :right                       0}]
    [:.back
     {:transform "rotateX(180deg)"}]]
   [selector
    [:.front
     {:transform "rotateX(180deg)"}]
    [:.back
     {:transform "rotateX(360deg)"}]]])

(defstyles login-button
  [:.login-button-container
   {:width         "200px"
    :height        "30px"
    :margin-bottom "16px"
    :cursor        :pointer}
   [:.login-button
    {:color         :white
     :font-family   "Helvetica Neue, sans-serif"
     :font-weight   :bold
     :white-space   :nowrap
     :height        "30px"
     :line-height   "30px"
     :width         "200px"
     :border-radius "15px"
     :text-align    :center
     :overflow      :hidden
     :text-overflow :ellipsis
     :cursor        :pointer}
    [:img
     {:position :absolute}]
    [:&.back
     {:background-color :black}]]
   (flipping :&.enabled:hover)
   (when-desktop
    [:&
     {:margin-top "32px"
      :float      :right}])])

(defstyles spotify-login-button
  [:.spotify
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
   {:margin-right "16px"}
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
   [:.progress-bar-container
    {:float  :left
     :height "32px"}]
   [:.tag-filter
    {:float        :left
     :margin-right "12px"}]
   [:.text-search
    {:float         :left
     :padding-right "48px"
     :position      :relative}]
   [:.sort-container
    {:float         :left
     :padding-right "48px"
     :position      :relative}]
   [:.selected-tags
    {:float      :left
     :clear      :left
     :display    :flex
     :flex-wrap  :wrap
     :min-height "48px"}]
   (when-desktop
    [:&
     {:padding "0 16px"}
     [:.progress-bar-container
      {:width         "256px"
       :margin-bottom "16px"}]]
    [:&.empty
     [:.login-button-container
      {:margin-top "16px"}]])
   (when-mobile
    [:&
     {:padding "16px"}
     [:.progress-bar-container
      {:width      "calc(100vw - 80px)"
       :margin-top "8px"}]]
    [:&.empty
     [:.login-button-container
      {:margin 0}]])
   login-button
   spotify-login-button
   lastfm-login-button])

(defstyles last-fm-erase-button
  [:.erase-data
   {:display :inline-block
    :width   "240px"
    :height  "24px"
    :cursor  :pointer}
   [:.button
    {:width         "240px"
     :height        "24px"
     :line-height   "22px"
     :text-align    :center
     :border-radius "12px"
     :border        "1px solid #cccccc"}
    [:&.back
     {:background-color "#fdb8c0"}]
    [:&.done
     {:background-color "#acf2bd"}]]
   [:&.disabled
    {:cursor :default}]
   (flipping :&.confirm)])

(defstyles terms-of-use
  [:.terms-of-use
   {:font-size  "20px"
    :text-align :center
    :color      "#666666"}
   [:a
    {:color :black}]
   [:img
    {:height         "1em"
     :vertical-align :baseline}]]
  [:.terms-of-use-dialog
   last-fm-erase-button
   [:a
    {:color :inherit}
    [:&:hover
     {:text-decoration :underline}]]])

(defstyles header
  [:#header
   {:padding   "16px"
    :font-size "24px"}])

(defstyles screen
  (at-import "https://fonts.googleapis.com/css?family=Roboto:300,400,500")
  [:body {:font-family "Roboto, sans-serif"
          :margin      0}]
  [:* {:box-sizing :border-box}]
  [:a {:text-decoration :none}]
  [:#content
   {:min-height "1000px"}]
  album-list
  toolbar
  header
  terms-of-use)

(def css (garden/css screen))
