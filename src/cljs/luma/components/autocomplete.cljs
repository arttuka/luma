(ns luma.components.autocomplete
  (:require [reagent.core :as reagent :refer [atom]]
            [clojure.string :as str]
            [goog.object :as obj]
            [reagent-material-ui.components :as ui]
            [reagent-material-ui.util :refer [adapt-react-class js->clj' use-state]]
            [reagent-material-ui.lab.use-autocomplete :refer [use-autocomplete]]
            [reagent-material-ui.lab.create-filter-options :refer [create-filter-options]]))

(def max-results 10)

(defn input [{:keys [input-props] :as props}]
  (let [{:keys [on-blur on-change on-focus ref]} input-props]
    [ui/text-field (merge props {:InputProps  {:on-blur   on-blur
                                               :on-change on-change
                                               :on-focus  on-focus}
                                 :input-props (dissoc input-props :on-blur :on-change :on-focus :ref)
                                 :input-ref   ref})]))

(def filter-options (create-filter-options {:match-from :start
                                            :stringify  identity}))

(defn react-autocomplete [params]
  (let [{:keys [classes label on-select placeholder]} (js->clj' params)
        on-change (fn [_ v]
                    (on-select v))
        [open set-open] (use-state false)
        on-open (fn [e]
                  (when-not (str/blank? (.. e -target -value))
                    (set-open true)))
        on-close (fn [_]
                   (set-open false))
        {:keys [anchor-el
                get-input-label-props
                get-input-props
                get-listbox-props
                get-option-props
                get-root-props
                grouped-options
                popup-open
                set-anchor-el]} (use-autocomplete {:options                 (obj/get params "options")
                                                   :on-change               on-change
                                                   :on-open                 on-open
                                                   :on-close                on-close
                                                   :open                    open
                                                   :multiple                true
                                                   :filter-options          filter-options
                                                   :filter-selected-options true
                                                   :value                   (obj/get params "value")})]
    (reagent/as-element
     [:div (merge (get-root-props)
                  {:class (:root classes)
                   :ref   set-anchor-el})
      [input {:full-width      true
              :InputLabelProps (assoc (get-input-label-props) :shrink true)
              :input-props     (get-input-props)
              :label           label
              :placeholder     placeholder}]
      [ui/popper {:open      popup-open
                  :anchor-el anchor-el
                  :placement :bottom-start}
       [ui/paper {:class (:popup classes)}
        [ui/menu-list (get-listbox-props)
         (for [[index option] (map-indexed vector (take max-results grouped-options))]
           ^{:key option}
           (let [option-props (get-option-props {:index  index
                                                 :option option})]
             [ui/menu-item (assoc option-props :class (:menu-item classes))
              [ui/typography {:variant :inherit
                              :no-wrap true}
               option]]))
         (when (< max-results (count grouped-options))
           [ui/menu-item {:disabled true}
            "···"])]]]])))

(def autocomplete (adapt-react-class react-autocomplete))
