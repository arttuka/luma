(ns luma.styles.main
  (:require [garden.def :refer [defstyles]]
            [garden.core :as garden]))

(defstyles album-list
  [:#albums
   {:display   :flex
    :flex-wrap :wrap}
   [:a
    {:text-decoration :none
     :margin          "0 auto 40px"}]
   [:.album-card
    {:width "320px"}
    [:.title
     {:font-size     "24px"
      :font-weight   :bold
      :margin-bottom "5px"}]
    [:.artist
     {:font-size "16px"}]]])

(defstyles spotify-login-button
  [:.login
   {:background-color "#1db954"
    :color            :white
    :text-decoration  :none
    :font-family      "Helvetica Neue, sans-serif"
    :font-weight      :bold
    :display          :inline-block
    :height           "30px"
    :line-height      "30px"
    :border-radius    "15px"
    :padding-right    "6px"
    :float :right}
   [:img
    {:height "24px"
     :float  :left
     :margin "3px"}]
   [:div
    {:display        :inline-block
     :vertical-align :middle}]])

(defstyles toolbar
  [:#toolbar
   {:padding       "10px"
    :margin-bottom "20px"}
   [:.selected-tags
    {:display :flex
     :flex-wrap :wrap
     :min-height "32px"}]]
  spotify-login-button)

(defstyles screen
  [:body {:font-family :sans-serif}]
  [:* {:box-sizing :border-box}]
  album-list
  toolbar)

(def css (garden/css screen))
