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

(defstyles toolbar
  [:#toolbar
   {:padding       "10px"
    :margin-bottom "20px"}
   [:.selected-tags
    {:display :flex
     :flex-wrap :wrap
     :min-height "32px"}]])

(defstyles screen
  [:body {:font-family :sans-serif}]
  album-list
  toolbar)

(def css (garden/css screen))
