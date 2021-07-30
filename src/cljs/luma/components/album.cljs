(ns luma.components.album
  (:require [reagent.core :as reagent :refer [atom with-let]]
            [re-frame.core :as re-frame]
            [reagent-material-ui.core.button :refer [button]]
            [reagent-material-ui.core.card :refer [card]]
            [reagent-material-ui.core.card-action-area :refer [card-action-area]]
            [reagent-material-ui.core.card-content :refer [card-content]]
            [reagent-material-ui.core.card-media :refer [card-media]]
            [reagent-material-ui.core.circular-progress :refer [circular-progress]]
            [reagent-material-ui.core.typography :refer [typography]]
            [reagent-material-ui.styles :refer [styled]]
            [clojure.string :as str]
            [luma.subs :as subs]
            [luma.util :as util]))

(defn lastfm-url [album]
  (str "https://www.last.fm/music/"
       (str/replace (:name (first (:artists album))) \space \+)
       "/"
       (str/replace (:title album) \space \+)))

(def classes
  (util/make-classes "luma-album-"
                     [:bold
                      :playcount-root
                      :progress-root
                      :progress-svg
                      :root
                      :album-card
                      :album-link
                      :album-media]))

(defn styles [{:keys [theme]}]
  (let [spacing (:spacing theme)
        on-desktop (util/on-desktop theme)
        on-mobile (util/on-mobile theme)
        mobile-width (str "calc(100vw - " (spacing 4) ")")]
    (util/set-classes
     classes
     {:bold           {:font-weight :bold}
      :playcount-root {:z-index  2
                       :position :absolute
                       :top      5
                       :right    5}
      :progress-root  {:margin-top (spacing 4)
                       on-mobile   {:display :block
                                    :margin  [[(spacing 4) :auto 0]]}}
      :progress-svg   {:stroke-linecap :round}
      :root           {:padding   (spacing 0 2)
                       on-desktop {:display         :flex
                                   :flex-wrap       :wrap
                                   :justify-content :center}}
      :album-card     {:display        :flex
                       :flex-direction :column
                       :position       :relative
                       on-desktop      {:width  320
                                        :margin (spacing 1)}
                       on-mobile       {:width  mobile-width
                                        :margin (spacing 2 0)}}
      :album-link     {:flex 1}
      :album-media    {on-desktop {:height 320}
                       on-mobile  {:height mobile-width}}})))

(defn album [{:keys [data]}]
  (with-let [depth (atom 1)
             on-mouse-over #(reset! depth 10)
             on-mouse-out #(reset! depth 1)]
    [card {:classes       {:root (:album-card classes)}
           :on-mouse-over on-mouse-over
           :on-mouse-out  on-mouse-out
           :elevation     @depth}
     (when-let [playcount (:playcount data)]
       [button {:classes {:root (:playcount-root classes)}
                :color   :secondary
                :variant :contained
                :size    :small
                :href    (lastfm-url data)}
        [:span [:span {:class (:bold classes)} playcount] " scrobbles"]])
     [card-action-area {:classes {:root (:album-link classes)}
                        :href    (:uri data)}
      [card-media {:classes {:root (:album-media classes)}
                   :image   (:image data)}]
      [card-content
       [typography {:variant :h5
                    :no-wrap true}
        (:title data)]
       [typography {:color         :textSecondary
                    :no-wrap       true
                    :gutter-bottom true}
        (interpose " · " (map :name (:artists data)))]
       [typography {:variant :body2}
        (interpose " · " (:tags data))]]]]))

(defn albums* [{:keys [class-name]}]
  (with-let [has-albums? (re-frame/subscribe [::subs/albums])
             filtered-albums (re-frame/subscribe [::subs/sorted-albums])]
    [:div {:class [class-name (:root classes)]}
     (cond
       (not @has-albums?) [circular-progress {:classes   {:root (:progress-root classes)
                                                          :svg  (:progress-svg classes)}
                                              :size      100
                                              :thickness 5}]
       (seq @filtered-albums) (for [a @filtered-albums]
                                ^{:key (:id a)}
                                [album {:classes classes
                                        :data    a}])
       :else [typography {:variant :h6}
              "No matching albums found in your library."])]))

(def albums (styled albums* styles))
