(ns luma.css
  (:require [garden.def :refer [defstyles]]))

(defstyles album
  [:& {:border  "1px solid #444444"
       :padding "5px"}
   [:&:hover {:background-color "#cccccc"}]])

(defstyles album-list
  [:#albums
   [:.album
    album
    {:display         :block
     :min-height      "160px"
     :margin-bottom   "10px"
     :text-decoration :none
     :color           "#333333"}
    [:.title
     {:font-size     "32px"
      :font-weight   :bold
      :margin-bottom "5px"}]
    [:.artist
     {:font-size "20px"}]
    [:img
     {:height       "160px"
      :float        :left
      :margin-right "20px"}]]])

(defstyles screen
  [:body {:color       "red"
          :padding     "50px"
          :font-family :sans-serif}]
  album-list)

