(ns luma.components.autocomplete
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent-material-ui.components :as ui]
            [clojure.string :as str]
            [oops.core :refer [oget]]
            [luma.components.downshift :as downshift]))

(def max-results 10)

(defn input [{:keys [input-props] :as props}]
  (let [{:keys [on-blur on-change on-key-down]} input-props]
    [ui/text-field (merge props {:InputProps  {:on-blur     on-blur
                                               :on-change   on-change
                                               :on-key-down on-key-down}
                                 :input-props (dissoc input-props :on-blur :on-change :on-key-down)})]))

(defn autocomplete [{:keys [datasource on-select]}]
  (let [{:keys [key-down-enter click-item]} downshift/state-change-types
        state-reducer (fn [_ changes]
                        (condp contains? (:type changes)
                          #{click-item key-down-enter} (assoc changes
                                                              :input-value ""
                                                              :selected-item nil)
                          changes))
        popper-anchor (.createRef js/React)]
    (fn [{:keys [classes label placeholder]}]
      [downshift/component {:on-select     on-select
                            :selected-item nil
                            :state-reducer state-reducer}
       (fn downshift-render [downshift-props]
         (let [{:keys [get-input-props
                       get-label-props
                       get-menu-props
                       get-item-props
                       highlighted-index
                       open?
                       input-value]} downshift-props
               items (when (and @datasource (not (str/blank? input-value)))
                       (@datasource (str/lower-case input-value)))
               anchor-el (.-current popper-anchor)
               menu-open? (boolean (and open? (seq items)))]
           (reagent/as-element
            [:div {:class (:root classes)}
             [input {:full-width      true
                     :input-props     (get-input-props)
                     :InputLabelProps (get-label-props {:shrink true})
                     :input-ref       popper-anchor
                     :label           label
                     :placeholder     placeholder}]
             [ui/popper {:open      menu-open?
                         :anchor-el anchor-el
                         :placement :bottom-start
                         :class     (:menu classes)}
              [ui/paper {:style {:width (some-> anchor-el
                                                (oget "clientWidth"))}}
               [ui/menu-list (if menu-open? (get-menu-props {} {:suppress-ref-error true}) {})
                (for [[index item] (map-indexed vector (take max-results items))]
                  ^{:key item}
                  [ui/menu-item (get-item-props {:index    index
                                                 :item     item
                                                 :selected (= highlighted-index index)})
                   [ui/typography {:variant :inherit
                                   :no-wrap true}
                    item]])
                (when (< max-results (count items))
                  [ui/menu-item {:disabled true}
                   "···"])]]]])))])))

