(ns luma.components.autosuggest
  (:require [reagent.core :as reagent :refer [atom]]
            [cljsjs.material-ui]
            [cljs-react-material-ui.reagent :as ui]
            [cljsjs.react-autosuggest]))

(defn ^:private suggestion [suggestion opts]
  (reagent/as-element
    [ui/menu-item {:primaryText suggestion}]))

(defn ^:private suggestion-container [props]
  (reagent/as-element
    [ui/paper (js->clj (.-containerProps props)) (.-children props)]))

(defn ^:private input [props]
  (let [props (js->clj props)]
    (reagent/as-element
      [ui/text-field props])))

(def ^:private styles
  {:container                {:flexGrow 1
                              :position "relative"
                              :width    200}
   :suggestionsContainerOpen {:position "absolute"
                              :zIndex   1
                              :left     0
                              :right    0}
   :suggestion               {:display "block"}
   :suggestionsList          {:margin        0
                              :padding       0
                              :listStyleType "none"}
   :suggestionHighlighted    {:background-color "rgba(0, 0, 0, 0.1)"}})

(defn autosuggest [{:keys [datasource on-change] :as options}]
  (let [suggestions (atom [])
        value (atom "")]
    (fn [{:keys [datasource on-change] :as options}]
      [:> js/Autosuggest {:suggestions                 @suggestions
                          :onSuggestionsFetchRequested (fn [event]
                                                         (reset! suggestions (datasource (.-value event))))
                          :onSuggestionsClearRequested #(reset! suggestions [])
                          :getSuggestionValue          identity
                          :renderSuggestionsContainer  suggestion-container
                          :renderSuggestion            suggestion
                          :renderInputComponent        input
                          :inputProps                  {:onChange (fn [event new-value]
                                                                    (condp contains? (.-method new-value)
                                                                      #{"click" "enter"} (do
                                                                                           (on-change (.-newValue new-value))
                                                                                           (reset! value ""))
                                                                      #{"type"} (reset! value (.-value (.-target event)))
                                                                      nil))
                                                        :value    @value}
                          :theme                       styles}])))
