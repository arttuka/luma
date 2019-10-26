(ns luma.styles.main
  (:require [garden.def :refer [defstyles]]
            [garden.core :as garden]
            [garden.stylesheet :refer [at-import]]
            [luma.util :refer [when-desktop when-mobile]]))

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
  header
  terms-of-use)

(def css (garden/css screen))
