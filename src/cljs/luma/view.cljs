(ns luma.view
  (:require [reagent-material-ui.components :as ui]
            [reagent-material-ui.icons.sort-by-alpha :refer [sort-by-alpha]]
            [reagent-material-ui.icons.cancel :refer [cancel]]
            [reagent-material-ui.styles :as styles]
            [clojure.string :as str]
            [reagent.core :as reagent :refer [atom]]
            [re-frame.core :as re-frame]
            [goog.string :as gstring]
            [oops.core :refer [oget]]
            [luma.components.album :refer [albums]]
            [luma.components.terms-of-use :refer [terms-of-use]]
            [luma.components.toolbar :refer [toolbar]]
            [luma.events :as events]
            [luma.subs :as subs]
            [luma.util :refer [debounce wrap-on-change]]
            [luma.websocket :as ws]))

(defn welcome-screen []
  [ui/paper {:style {:max-width  "600px"
                     :padding    "10px"
                     :margin-top "16px"
                     :height     "100%"}}
   [:h2 "LUMA Ultimate Music Archive"]
   [:p "Welcome to LUMA, a music archive that helps you sort your Spotify Music Library."]
   [:p "To begin, login with your Spotify account. Loading the tags for your albums will take some time on the first login."]
   [:p "You can also login with your Last.fm account to see play counts for the albums. Loading the playcounts will take a lot of time on the first login."]
   [:p "By logging in, you allow LUMA to use and process data about your Spotify and Last.fm accounts. For more information, see terms of use."]])


(defn header []
  [:div#header {:style {:color :white}}
   "LUMA Ultimate Music Archive"])

(defn snackbar []
  (let [error (re-frame/subscribe [::subs/error])]
    (fn snackbar-render []
      [ui/snackbar {:open                (boolean @error)
                    :on-request-close    #(re-frame/dispatch [::events/close-error])
                    :message             (or (:msg @error) "")
                    :action              (when (:retry-event @error) "retry")
                    :on-action-touch-tap #(do (re-frame/dispatch [::events/close-error])
                                              (re-frame/dispatch [::ws/send [(:retry-event @error)]]))}])))

(defn main-panel []
  [styles/theme-provider (styles/create-mui-theme {})
   [:div
    [header]
    [toolbar]
    [:div#content
     [albums]]
    [terms-of-use]
    [snackbar]]])
