(ns luma.components.toolbar
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :as re-frame]
            [reagent-material-ui.components :as ui]
            [reagent-material-ui.icons.sort :refer [sort]]
            [reagent-material-ui.icons.cancel :refer [cancel]]
            [reagent-material-ui.styles :refer [with-styles]]
            [clojure.string :as str]
            [goog.string :as gstring]
            [luma.components.autocomplete :refer [autocomplete]]
            [luma.components.flip-button :refer [flip-button]]
            [luma.events :as events]
            [luma.subs :as subs]
            [luma.util :as util :refer [debounce wrap-on-change]]))

(defn spotify-login []
  (let [uid (re-frame/subscribe [::subs/uid])
        spotify-id (re-frame/subscribe [::subs/spotify-id])
        client-id (re-frame/subscribe [::subs/env :spotify-client-id])
        redirect-uri (re-frame/subscribe [::subs/env :spotify-redirect-uri])
        response-type "code"
        scopes "user-library-read"
        icon (reagent/as-element [:img {:src "/images/Spotify_Icon_RGB_White.png"}])]
    (fn []
      [flip-button {:class-name :spotify
                    :front      (if @spotify-id
                                  {:icon  icon
                                   :label @spotify-id}
                                  {:icon  icon
                                   :label "Login with Spotify"
                                   :href  (gstring/format "https://accounts.spotify.com/authorize?client_id=%s&response_type=%s&state=%s&scope=%s&redirect_uri=%s"
                                                          @client-id response-type @uid scopes @redirect-uri)})
                    :back       (when @spotify-id
                                  {:icon  icon
                                   :label "Log out"
                                   :href  "/logout"})}])))

(defn lastfm-login []
  (let [lastfm-id (re-frame/subscribe [::subs/lastfm-id])
        api-key (re-frame/subscribe [::subs/env :lastfm-api-key])
        redirect-uri (re-frame/subscribe [::subs/env :lastfm-redirect-uri])
        icon (reagent/as-element [:img {:src "/images/Last.fm_Logo_White.png"}])]
    (fn []
      [flip-button {:class-name :lastfm
                    :front      (if @lastfm-id
                                  {:icon  icon
                                   :label @lastfm-id}
                                  {:icon  icon
                                   :label "Login"
                                   :href  (gstring/format "http://www.last.fm/api/auth/?api_key=%s&cb=%s"
                                                          @api-key @redirect-uri)})
                    :back       (when @lastfm-id
                                  {:icon  icon
                                   :label "Log out"
                                   :href  "/logout"})}])))

(defn progress-bar [props]
  (let [progress (re-frame/subscribe [::subs/progress])
        albums (re-frame/subscribe [::subs/albums])]
    (fn [{:keys [class]}]
      [ui/linear-progress {:class class
                           :mode  (if (seq @albums)
                                    :determinate
                                    :indeterminate)
                           :max   100
                           :min   0
                           :value @progress}])))

(defn selected-tags [props]
  (let [selected-tags (re-frame/subscribe [::subs/selected-tags])
        all-tags (re-frame/subscribe [::subs/all-tags])]
    (fn [{:keys [classes]}]
      (if @all-tags
        [:<>
         (for [tag @selected-tags]
           ^{:key (str "selected-tag-" tag)}
           [ui/chip {:classes   {:root  (:chip classes)
                                 :label (:chip-label classes)}
                     :on-delete #(re-frame/dispatch [::events/unselect-tag tag])
                     :label     (reagent/as-element
                                 [ui/typography {:no-wrap   true
                                                 :component :span
                                                 :variant   :inherit}
                                  tag])}])]
        [:div {:class (:progress-container classes)}
         [ui/typography
          "Loading tags..."]
         [progress-bar {:class (:progress classes)}]]))))

(defn tag-filter [props]
  (let [all-tags (re-frame/subscribe [::subs/all-tags])
        on-select #(re-frame/dispatch [::events/select-tag %])]
    (fn [{:keys [classes]}]
      [autocomplete {:classes     classes
                     :datasource  all-tags
                     :label       "Tag search"
                     :on-select   on-select
                     :placeholder "Tag"}])))

(defn sort-dropdown [props]
  (let [lastfm-id (re-frame/subscribe [::subs/lastfm-id])
        sort-key (re-frame/subscribe [::subs/sort-key])
        sort-asc (re-frame/subscribe [::subs/sort-asc])
        value (atom @sort-key)
        on-change (wrap-on-change
                   (fn [new-value]
                     (reset! value new-value)
                     (re-frame/dispatch [::events/sort-albums (keyword new-value)])))
        on-click #(re-frame/dispatch [::events/change-sort-dir])]
    (fn [{:keys [classes]}]
      [:div {:class (:root classes)}
       [ui/form-control {:full-width true}
        [ui/input-label {:html-for "sort-select"}
         "Sort by"]
        [ui/select {:input-props {:id   "sort-select"
                                  :name "sort-select"}
                    :value       @value
                    :on-change   on-change}
         [ui/menu-item {:value :artist}
          "Artist"]
         [ui/menu-item {:value :album}
          "Album title"]
         [ui/menu-item {:value :added}
          "Added at"]
         (when @lastfm-id
           [ui/menu-item {:value :playcount}
            "Scrobbles"])]]
       [ui/icon-button {:on-click on-click
                        :color    (if @sort-asc :primary :secondary)}
        [sort {:class (when @sort-asc (:asc-icon classes))}]]])))

(defn text-search [props]
  (let [db-value (re-frame/subscribe [::subs/text-search])
        value (atom @db-value)
        delayed-dispatch (debounce (fn [v] (re-frame/dispatch [::events/set-text-search v]))
                                   250)
        on-change (wrap-on-change
                   (fn [v]
                     (reset! value v)
                     (delayed-dispatch v)))
        reset-value (fn []
                      (reset! value "")
                      (re-frame/dispatch [::events/set-text-search ""]))
        prevent-default (fn [event]
                          (.preventDefault event))]
    (fn [{:keys [classes]}]
      [ui/text-field
       {:classes         classes
        :label           "Free text search"
        :placeholder     "Title or artist"
        :value           @value
        :on-change       on-change
        :InputLabelProps {:shrink true}
        :InputProps      {:end-adornment (when-not (str/blank? @value)
                                           (reagent/as-element
                                            [ui/input-adornment {:position :end}
                                             [ui/icon-button
                                              {:on-click      reset-value
                                               :on-mouse-down prevent-default}
                                              [cancel]]]))}}])))

(defn styles [{:keys [spacing] :as theme}]
  (let [on-desktop (util/on-desktop theme)
        on-mobile (util/on-mobile theme)]
    {:root                  {:padding-top (spacing 2)}
     :filter                {on-desktop {:width        256
                                         :margin-right (spacing 1)}
                             on-mobile  {:width  "100%"
                                         :margin (spacing 1 0)}}
     :selected-tags-toolbar {on-mobile {:flex-wrap :wrap}}
     :progress              {:margin-top (spacing 1)}
     :chip                  {on-desktop {:margin-right (spacing 1)}
                             on-mobile  {:margin (spacing 0 1 1 0)}}
     :chip-label            {:max-width 280}
     :asc-icon              {:transform "scaleY(-1)"}
     :tag-filter-menu       {:z-index 2}
     :sort-dropdown-root    {:display     :flex
                             :align-items :flex-end}
     :separator             {on-desktop {:flex 1}}
     :toolbar               {on-mobile {:flex-direction :column}}}))

(defn toolbar* [props]
  (let [spotify-id (re-frame/subscribe [::subs/spotify-id])]
    (fn [{:keys [classes]}]
      [:div {:class (:root classes)}
       [ui/toolbar {:classes {:root (:toolbar classes)}}
        (if @spotify-id
          [:<>
           [tag-filter {:classes {:root (:filter classes)
                                  :menu (:tag-filter-menu classes)}}]
           [text-search {:classes {:root (:filter classes)}}]
           [sort-dropdown {:classes {:root     [(:sort-dropdown-root classes) (:filter classes)]
                                     :asc-icon (:asc-icon classes)}}]
           [:div {:class (:separator classes)}]
           [lastfm-login]]
          [:div {:class (:separator classes)}])
        [spotify-login]]
       [ui/toolbar {:class (:selected-tags-toolbar classes)}
        (when @spotify-id
          [selected-tags {:classes {:progress-container (:filter classes)
                                    :progress           (:progress classes)
                                    :chip               (:chip classes)
                                    :chip-label         (:chip-label classes)}}])]
       [ui/divider]])))

(def toolbar ((with-styles styles) toolbar*))
