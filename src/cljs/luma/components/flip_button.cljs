(ns luma.components.flip-button
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent-material-ui.core.button :refer [button]]
            [reagent-material-ui.styles :refer [styled]]
            [luma.util :as util]))

(def classes (util/make-classes
              "luma-flip-button-"
              [:icon
               :face
               :back
               :front
               :root
               :enabled-container]))

(defn styles [{:keys [theme]}]
  (let [{:keys [palette spacing]} theme
        icon-img (str "& ." (:icon classes) " img")
        hover #(str "&:hover ." (get classes %))
        enabled (str "&." (:enabled-container classes))]
    (util/set-classes
     classes
     {:icon   {:position :absolute
               :top      6
               :left     8
               :margin   0}
      :face   {:transform-style             :preserve-3d
               :transition                  "transform 0.5s linear"
               :backface-visibility         :hidden
               :-webkit-backface-visibility :hidden
               :position                    :absolute
               :top                         0
               :right                       0
               :color                       :white
               "&.spotify"                  {:padding-left 40
                                             icon-img      {:height 24}}
               "&.lastfm"                   {:padding-left 90
                                             icon-img      {:height 22
                                                            :margin "1px 0"}}}
      :back   {:transform        "rotateX(180deg)"
               :background-color :black
               "&:hover"         {:background-color :black}}
      :front  {"&.spotify, &.spotify:hover" {:background-color (:spotify palette)}
               "&.lastfm, &.lastfm:hover"   {:background-color (get-in palette [:secondary :main])}}
      :root   {:position               :relative
               :height                 36
               :width                  210
               (util/on-desktop theme) {:margin-left (spacing 1)}
               (util/on-mobile theme)  {:align-self :flex-start
                                        :margin     (spacing 1 0)
                                        "&.spotify" {:order -2}
                                        "&.lastfm"  {:order -1}}}
      enabled {(hover :front) {:transform "rotateX(180deg)"}
               (hover :back)  {:transform "rotateX(360deg)"}}})))

(defn flip-button* [{:keys [class-name button-class front back]}]
  (let [flip-enabled? (some? back)]
    [:div {:class [(:root classes)
                   class-name
                   button-class
                   (when flip-enabled? (:enabled-container classes))]}
     [button {:classes        {:root             (str (:face classes) " " (:front classes) " " button-class)
                               :icon-size-medium (:icon classes)}
              :href           (:href front)
              :start-icon     (:icon front)
              :disable-ripple true
              :full-width     true
              :variant        :contained}
      (:label front)]
     (when flip-enabled?
       [button {:classes        {:root             (str (:face classes) " " (:back classes) " " button-class)
                                 :icon-size-medium (:icon classes)}
                :href           (:href back)
                :start-icon     (:icon back)
                :disable-ripple true
                :full-width     true
                :variant        :contained}
        (:label back)])]))

(def flip-button (styled flip-button* styles))
