(ns luma.components.toolbar
  (:require [reagent.core :as reagent :refer [atom with-let]]
            [reagent.ratom :refer-macros [reaction]]
            [re-frame.core :as re-frame]
            [reagent-material-ui.core.chip :refer [chip]]
            [reagent-material-ui.core.divider :refer [divider]]
            [reagent-material-ui.core.form-control :refer [form-control]]
            [reagent-material-ui.core.icon-button :refer [icon-button]]
            [reagent-material-ui.core.input-adornment :refer [input-adornment]]
            [reagent-material-ui.core.input-label :refer [input-label]]
            [reagent-material-ui.core.linear-progress :refer [linear-progress]]
            [reagent-material-ui.core.menu-item :refer [menu-item]]
            [reagent-material-ui.core.select :refer [select]]
            [reagent-material-ui.core.text-field :refer [text-field]]
            [reagent-material-ui.core.toolbar :refer [toolbar] :rename {toolbar mui-toolbar}]
            [reagent-material-ui.core.typography :refer [typography]]
            [reagent-material-ui.icons.sort :refer [sort]]
            [reagent-material-ui.icons.cancel :refer [cancel]]
            [reagent-material-ui.lab.create-filter-options :refer [create-filter-options]]
            [reagent-material-ui.styles :refer [with-styles]]
            [clojure.string :as str]
            [goog.string :as gstring]
            [reagent-util.autocomplete :refer [autocomplete]]
            [luma.components.flip-button :refer [flip-button]]
            [luma.events :as events]
            [luma.subs :as subs]
            [luma.util :as util :refer [debounce wrap-on-change]]))

(defn spotify-login []
  (with-let [uid (re-frame/subscribe [::subs/uid])
             spotify-id (re-frame/subscribe [::subs/spotify-id])
             client-id (re-frame/subscribe [::subs/env :spotify-client-id])
             redirect-uri (re-frame/subscribe [::subs/env :spotify-redirect-uri])
             response-type "code"
             scopes "user-library-read"
             icon (reagent/as-element [:img {:src "/images/Spotify_Icon_RGB_White.png"}])]
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
                                 :href  "/logout"})}]))

(defn lastfm-login []
  (with-let [lastfm-id (re-frame/subscribe [::subs/lastfm-id])
             api-key (re-frame/subscribe [::subs/env :lastfm-api-key])
             redirect-uri (re-frame/subscribe [::subs/env :lastfm-redirect-uri])
             icon (reagent/as-element [:img {:src "/images/Last.fm_Logo_White.png"}])]
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
                                 :href  "/logout"})}]))

(defn progress-bar [{:keys [class]}]
  [linear-progress {:class class
                    :mode  (if (seq @(re-frame/subscribe [::subs/albums]))
                             :determinate
                             :indeterminate)
                    :max   100
                    :min   0
                    :value @(re-frame/subscribe [::subs/progress])}])

(defn selected-tags [{:keys [classes]}]
  (if @(re-frame/subscribe [::subs/all-tags])
    [:<>
     (for [tag @(re-frame/subscribe [::subs/selected-tags])]
       ^{:key (str "selected-tag-" tag)}
       [chip {:classes   {:root  (:chip classes)
                          :label (:chip-label classes)}
              :on-delete #(re-frame/dispatch [::events/unselect-tag tag])
              :label     (reagent/as-element
                          [typography {:no-wrap   true
                                       :component :span
                                       :variant   :inherit}
                           tag])}])]
    [:div {:class (:progress-container classes)}
     [typography
      "Loading tags..."]
     [progress-bar {:class (:progress classes)}]]))

(defn tag-filter [{:keys [classes]}]
  (with-let [all-tags (re-frame/subscribe [::subs/all-tags])
             all-tags-js (reaction (clj->js (or (seq @all-tags) [])))
             selected-tags (re-frame/subscribe [::subs/selected-tags])
             selected-tags-js (reaction (clj->js @selected-tags))
             on-select #(re-frame/dispatch [::events/select-tags %])
             filter-options (create-filter-options {:match-from :start
                                                    :stringify  identity})]
    [autocomplete {:classes        classes
                   :options        @all-tags-js
                   :label          "Tag search"
                   :on-select      on-select
                   :placeholder    "Tag"
                   :value          @selected-tags-js
                   :max-results    10
                   :filter-options filter-options
                   :shrink-label   true}]))

(defn sort-dropdown [{:keys [classes]}]
  (with-let [lastfm-id (re-frame/subscribe [::subs/lastfm-id])
             sort-key (re-frame/subscribe [::subs/sort-key])
             sort-asc (re-frame/subscribe [::subs/sort-asc])
             value (atom @sort-key)
             on-change (wrap-on-change
                        (fn [new-value]
                          (reset! value new-value)
                          (re-frame/dispatch [::events/sort-albums (keyword new-value)])))
             on-click #(re-frame/dispatch [::events/change-sort-dir])]
    [:div {:class (:root classes)}
     [form-control {:full-width true}
      [input-label {:html-for "sort-select"}
       "Sort by"]
      [select {:input-props {:id   "sort-select"
                             :name "sort-select"}
               :value       @value
               :on-change   on-change}
       [menu-item {:value :artist}
        "Artist"]
       [menu-item {:value :album}
        "Album title"]
       [menu-item {:value :added}
        "Added at"]
       (when @lastfm-id
         [menu-item {:value :playcount}
          "Scrobbles"])]]
     [icon-button {:on-click on-click
                   :color    (if @sort-asc :primary :secondary)}
      [sort {:class (when @sort-asc (:asc-icon classes))}]]]))

(defn text-search [{:keys [classes]}]
  (with-let [db-value (re-frame/subscribe [::subs/text-search])
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
             prevent-default (fn [^js/Event event]
                               (.preventDefault event))]
    [text-field
     {:classes         classes
      :label           "Free text search"
      :placeholder     "Title or artist"
      :value           @value
      :on-change       on-change
      :InputLabelProps {:shrink true}
      :InputProps      {:end-adornment (when-not (str/blank? @value)
                                         (reagent/as-element
                                          [input-adornment {:position :end}
                                           [icon-button
                                            {:on-click      reset-value
                                             :on-mouse-down prevent-default}
                                            [cancel]]]))}}]))

(defn styles [{:keys [palette spacing] :as theme}]
  (let [on-desktop (util/on-desktop theme)
        on-mobile (util/on-mobile theme)
        filter-width {on-desktop {:width        256
                                  :margin-right (spacing 1)}
                      on-mobile  {:width  "100%"
                                  :margin (spacing 1 0)}}]
    {:root                  {:padding-top (spacing 2)}
     :filter                filter-width
     :selected-tags-toolbar {on-mobile {:flex-wrap :wrap}}
     :progress              {:margin-top (spacing 1)}
     :chip                  {on-desktop {:margin-right (spacing 1)}
                             on-mobile  {:margin (spacing 0 1 1 0)}}
     :chip-label            {:max-width 280}
     :asc-icon              {:transform "scaleY(-1)"}
     :tag-filter-popup      filter-width
     :tag-filter-menu-item  {"&[data-focus=\"true\"]" {:background-color (get-in palette [:action :selected])}}
     :sort-dropdown-root    {:display     :flex
                             :align-items :flex-end}
     :separator             {on-desktop {:flex 1}}
     :toolbar               {on-mobile {:flex-direction :column}}}))

(defn toolbar* [{:keys [classes]}]
  (with-let [spotify-id (re-frame/subscribe [::subs/spotify-id])]
    [:div {:class (:root classes)}
     [mui-toolbar {:classes {:root (:toolbar classes)}}
      (if @spotify-id
        [:<>
         [tag-filter {:classes {:root      (:filter classes)
                                :popup     (:tag-filter-popup classes)
                                :menu-item (:tag-filter-menu-item classes)}}]
         [text-search {:classes {:root (:filter classes)}}]
         [sort-dropdown {:classes {:root     [(:sort-dropdown-root classes) (:filter classes)]
                                   :asc-icon (:asc-icon classes)}}]
         [:div {:class (:separator classes)}]
         [lastfm-login]]
        [:div {:class (:separator classes)}])
      [spotify-login]]
     [mui-toolbar {:class (:selected-tags-toolbar classes)}
      (when @spotify-id
        [selected-tags {:classes {:progress-container (:filter classes)
                                  :progress           (:progress classes)
                                  :chip               (:chip classes)
                                  :chip-label         (:chip-label classes)}}])]
     [divider]]))

(def toolbar ((with-styles styles) toolbar*))
