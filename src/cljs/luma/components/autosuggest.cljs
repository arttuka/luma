(ns luma.components.autosuggest
  (:require [reagent.core :as reagent :refer [atom]]
            [cljsjs.material-ui]
            [cljs-react-material-ui.reagent :as ui]
            [cljsjs.react-autosuggest]
            [oops.core :refer [oget]]))

(defn ^:private suggestion [suggestion opts]
  (reagent/as-element
   [ui/menu-item {:primary-text (reagent/as-element
                                 [:div {:style {:white-space   :nowrap
                                                :text-overflow :ellipsis
                                                :overflow      :hidden}}
                                  suggestion])}]))

(defn ^:private suggestion-container [props]
  (reagent/as-element
   [ui/paper (js->clj (oget props "containerProps")) (oget props "children")]))

(defn ^:private input [props]
  (reagent/as-element
   [ui/text-field (js->clj props)]))

(def ^:private styles
  {:container                  {:flex-grow 1
                                :position  "relative"
                                :width     256}
   :suggestions-container-open {:position "absolute"
                                :z-index  10
                                :left     0
                                :right    0}
   :suggestion                 {:display "block"}
   :suggestions-list           {:margin          0
                                :padding         0
                                :list-style-type "none"}
   :suggestion-highlighted     {:background-color "rgba(0, 0, 0, 0.1)"}})

(defn autosuggest [{:keys [datasource on-change] :as options}]
  (let [suggestions (atom [])
        value (atom "")]
    (fn autosuggest-render [{:keys [datasource on-change] :as options}]
      (let [select-suggestion #(do
                                 (on-change %)
                                 (reset! value ""))
            input-props (merge (dissoc options :datasource :on-change)
                               {:on-change    (fn [event new-value]
                                                (when (= "type" (oget new-value "method"))
                                                  (reset! value (oget event "target" "value"))))
                                :on-key-press (fn [event]
                                                (when (and (= 13 (oget event "charCode"))
                                                           (some #{@value} @suggestions))
                                                  (.preventDefault event)
                                                  (select-suggestion @value)))
                                :value        @value})]
        [:> js/Autosuggest {:suggestions                    @suggestions
                            :on-suggestions-fetch-requested (fn [event]
                                                              (reset! suggestions (datasource (oget event "value"))))
                            :on-suggestions-clear-requested #(reset! suggestions [])
                            :on-suggestion-selected         (fn [_ suggestion]
                                                              (select-suggestion (oget suggestion "suggestion")))
                            :get-suggestion-value           identity
                            :render-suggestions-container   suggestion-container
                            :render-suggestion              suggestion
                            :render-input-component         input
                            :input-props                    input-props
                            :theme                          styles}]))))
