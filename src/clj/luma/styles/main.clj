(ns luma.styles.main
  (:require [garden.def :refer [defstyles]]
            [garden.core :as garden]))

(defstyles album-list
  [:#albums
   {:display   :flex
    :flex-wrap :wrap}
   [:a
    {:text-decoration :none
     :margin "0 auto 40px"}]
   [:.album-card
    {:width "320px"}
    [:.title
     {:font-size     "24px"
      :font-weight   :bold
      :margin-bottom "5px"}]
    [:.artist
     {:font-size "16px"}]]])

(defstyles screen
  [:body {:color       "red"
          :padding     "50px"
          :font-family :sans-serif}]
  album-list)

(def css (garden/css screen))
