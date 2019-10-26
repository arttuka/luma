(ns luma.styles.main
  (:require [garden.def :refer [defstyles]]
            [garden.core :as garden]
            [garden.stylesheet :refer [at-import]]
            [luma.util :refer [when-desktop when-mobile]]))

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
  header)

(def css (garden/css screen))
