(ns luma.components.autosuggest
  (:require [reagent.core :as reagent :refer [atom]]
            [cljsjs.material-ui]
            [cljs-react-material-ui.reagent :as ui]
            [cljsjs.react-autosuggest]))

(defn ^:private suggestion [suggestion opts]
  (reagent/as-element
    [ui/menu-item {:primary-text suggestion}]))

(defn ^:private suggestion-container [props]
  (reagent/as-element
    [ui/paper (js->clj (.-containerProps props)) (.-children props)]))

(defn ^:private input [props]
  (let [props (js->clj props)]
    (reagent/as-element
      [ui/text-field props])))

(def ^:private styles
  {:container                  {:flex-grow 1
                                :position  "relative"
                                :width     200}
   :suggestions-container-open {:position "absolute"
                                :z-index  1
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
      (let [input-props (merge (dissoc options :datasource :on-change)
                               {:onChange (fn [event new-value]
                                            (condp contains? (.-method new-value)
                                              #{"click" "enter"} (do
                                                                   (on-change (.-newValue new-value))
                                                                   (reset! value ""))
                                              #{"type"} (reset! value (.-value (.-target event)))
                                              nil))
                                :value    @value})]
        [:> js/Autosuggest {:suggestions                    @suggestions
                            :on-suggestions-fetch-requested (fn [event]
                                                              (reset! suggestions (datasource (.-value event))))
                            :on-suggestions-clear-requested #(reset! suggestions [])
                            :get-suggestion-value           identity
                            :render-suggestions-container   suggestion-container
                            :render-suggestion              suggestion
                            :render-input-component         input
                            :input-props                    input-props
                            :theme                          styles}]))))
