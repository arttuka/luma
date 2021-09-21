(ns luma.components.toolbar
  (:require-macros [reagent-mui.util :refer [react-component]])
  (:require [reagent.core :as reagent :refer [atom with-let]]
            [reagent.ratom :refer-macros [reaction]]
            [re-frame.core :as re-frame]
            [reagent-mui.material.autocomplete :refer [autocomplete]]
            [reagent-mui.material.chip :refer [chip]]
            [reagent-mui.material.create-filter-options :refer [create-filter-options]]
            [reagent-mui.material.divider :refer [divider]]
            [reagent-mui.material.form-control :refer [form-control]]
            [reagent-mui.material.icon-button :refer [icon-button]]
            [reagent-mui.material.input-adornment :refer [input-adornment]]
            [reagent-mui.material.input-label :refer [input-label]]
            [reagent-mui.material.linear-progress :refer [linear-progress]]
            [reagent-mui.material.menu-item :refer [menu-item]]
            [reagent-mui.material.select :refer [select]]
            [reagent-mui.material.text-field :refer [text-field]]
            [reagent-mui.material.toolbar :refer [toolbar] :rename {toolbar mui-toolbar}]
            [reagent-mui.material.typography :refer [typography]]
            [reagent-mui.icons.sort :refer [sort]]
            [reagent-mui.icons.cancel :refer [cancel]]
            [reagent-mui.styles :refer [styled]]
            [clojure.string :as str]
            [goog.string :as gstring]
            [luma.components.flip-button :refer [flip-button]]
            [luma.events :as events]
            [luma.subs :as subs]
            [luma.util :as util :refer [debounce wrap-on-change]]))

(def classes
  (util/make-classes "luma-toolbar"
                     [:root
                      :filter
                      :selected-tags-toolbar
                      :progress
                      :chip
                      :chip-label
                      :asc-icon
                      :sort-dropdown-root
                      :separator
                      :toolbar]))

(defn styles [{:keys [theme]}]
  (let [spacing (:spacing theme)
        on-desktop (util/on-desktop theme)
        on-mobile (util/on-mobile theme)
        filter-width {on-desktop {:width        256
                                  :margin-right (spacing 1)}
                      on-mobile  {:width  "100%"
                                  :margin (spacing 1 0)}}]
    (util/set-classes
     classes
     {:root                  {:padding-top (spacing 2)}
      :filter                filter-width
      :selected-tags-toolbar {on-mobile {:flex-wrap :wrap}}
      :progress              {:margin-top (spacing 1)}
      :chip                  {on-desktop {:margin-right (spacing 1)}
                              on-mobile  {:margin (spacing 0 1 1 0)}}
      :chip-label            {:max-width 280}
      :asc-icon              {:transform "scaleY(-1)"}
      :sort-dropdown-root    {:display     :flex
                              :align-items :flex-end}
      :separator             {on-desktop {:flex 1}}
      :toolbar               {on-mobile {:flex-direction :column}}})))

(defn spotify-login []
  (with-let [uid (re-frame/subscribe [::subs/uid])
             spotify-id (re-frame/subscribe [::subs/spotify-id])
             client-id (re-frame/subscribe [::subs/env :spotify-client-id])
             redirect-uri (re-frame/subscribe [::subs/env :spotify-redirect-uri])
             response-type "code"
             scopes "user-library-read"
             icon (reagent/as-element [:img {:src "/images/Spotify_Icon_RGB_White.png"}])]
    [flip-button {:button-class :spotify
                  :front        (if @spotify-id
                                  {:icon  icon
                                   :label @spotify-id}
                                  {:icon  icon
                                   :label "Login with Spotify"
                                   :href  (gstring/format "https://accounts.spotify.com/authorize?client_id=%s&response_type=%s&state=%s&scope=%s&redirect_uri=%s"
                                                          @client-id response-type @uid scopes @redirect-uri)})
                  :back         (when @spotify-id
                                  {:icon  icon
                                   :label "Log out"
                                   :href  "/logout"})}]))

(defn lastfm-login []
  (with-let [lastfm-id (re-frame/subscribe [::subs/lastfm-id])
             api-key (re-frame/subscribe [::subs/env :lastfm-api-key])
             redirect-uri (re-frame/subscribe [::subs/env :lastfm-redirect-uri])
             icon (reagent/as-element [:img {:src "/images/Last.fm_Logo_White.png"}])]
    [flip-button {:button-class :lastfm
                  :front        (if @lastfm-id
                                  {:icon  icon
                                   :label @lastfm-id}
                                  {:icon  icon
                                   :label "Login"
                                   :href  (gstring/format "http://www.last.fm/api/auth/?api_key=%s&cb=%s"
                                                          @api-key @redirect-uri)})
                  :back         (when @lastfm-id
                                  {:icon  icon
                                   :label "Log out"
                                   :href  "/logout"})}]))

(defn progress-bar []
  [linear-progress {:class (:progress classes)
                    :mode  (if (seq @(re-frame/subscribe [::subs/albums]))
                             :determinate
                             :indeterminate)
                    :max   100
                    :min   0
                    :value @(re-frame/subscribe [::subs/progress])}])

(defn selected-tags []
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
    [:div {:class (:filter classes)}
     [typography
      "Loading tags..."]
     [progress-bar]]))

(defn tag-filter []
  (with-let [all-tags (re-frame/subscribe [::subs/all-tags])
             selected-tags (re-frame/subscribe [::subs/selected-tags])
             on-change #(re-frame/dispatch [::events/select-tags %2])
             filter-options (create-filter-options {:match-from :start
                                                    :limit      10})]
    [autocomplete {:classes                 {:root (:filter classes)}
                   :options                 (or @all-tags [])
                   :value                   @selected-tags
                   :on-change               on-change
                   :multiple                true
                   :disable-clearable       true
                   :render-input            (react-component [params]
                                              [text-field (-> params
                                                              (merge {:variant     :standard
                                                                      :label       "Tag search"
                                                                      :placeholder "Tag"})
                                                              (assoc-in [:InputLabelProps :shrink] true))])
                   :render-tags             (constantly nil)
                   :filter-options          filter-options
                   :filter-selected-options true}]))

(defn sort-dropdown []
  (with-let [lastfm-id (re-frame/subscribe [::subs/lastfm-id])
             sort-key (re-frame/subscribe [::subs/sort-key])
             sort-asc (re-frame/subscribe [::subs/sort-asc])
             value (atom @sort-key)
             on-change (wrap-on-change
                        (fn [new-value]
                          (reset! value new-value)
                          (re-frame/dispatch [::events/sort-albums (keyword new-value)])))
             on-click #(re-frame/dispatch [::events/change-sort-dir])]
    [:div {:class [(:sort-dropdown-root classes) (:filter classes)]}
     [form-control {:full-width true
                    :variant    :standard}
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

(defn text-search []
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
     {:classes         {:root (:filter classes)}
      :label           "Free text search"
      :placeholder     "Title or artist"
      :value           @value
      :on-change       on-change
      :variant         :standard
      :InputLabelProps {:shrink true}
      :InputProps      {:end-adornment (when-not (str/blank? @value)
                                         (reagent/as-element
                                          [input-adornment {:position :end}
                                           [icon-button
                                            {:on-click      reset-value
                                             :on-mouse-down prevent-default}
                                            [cancel]]]))}}]))

(defn toolbar* [{:keys [class-name]}]
  (with-let [spotify-id (re-frame/subscribe [::subs/spotify-id])]
    [:div {:class [class-name (:root classes)]}
     [mui-toolbar {:classes {:root (:toolbar classes)}}
      (if @spotify-id
        [:<>
         [tag-filter]
         [text-search]
         [sort-dropdown]
         [:div {:class (:separator classes)}]
         [lastfm-login]]
        [:div {:class (:separator classes)}])
      [spotify-login]]
     [mui-toolbar {:class (:selected-tags-toolbar classes)}
      (when @spotify-id
        [selected-tags])]
     [divider]]))

(def toolbar (styled toolbar* styles))
