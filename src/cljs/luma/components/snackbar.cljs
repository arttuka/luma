(ns luma.components.snackbar
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :as re-frame]
            [reagent-material-ui.core.button :refer [button]]
            [reagent-material-ui.core.snackbar :refer [snackbar] :rename {snackbar mui-snackbar}]
            [reagent-material-ui.core.snackbar-content :refer [snackbar-content]]
            [reagent-material-ui.icons.error :refer [error] :rename {error error-icon}]
            [reagent-material-ui.styles :refer [with-styles]]
            [luma.events :as events]
            [luma.subs :as subs]
            [luma.websocket :as ws]))

(defn styles [{:keys [palette spacing]}]
  {:root    {:background-color (get-in palette [:error :dark])}
   :message {:display     :flex
             :align-items :center}
   :icon    {:margin-right (spacing 1)}
   :button  {:color :white}})

(defn snackbar* [props]
  (let [error (re-frame/subscribe [::subs/error])
        close #(re-frame/dispatch [::events/close-error])
        retry #(do (re-frame/dispatch [::events/close-error])
                   (ws/send! [(:retry-event @error)]))]
    (fn [{:keys [classes]}]
      [mui-snackbar {:open     (boolean @error)
                     :on-close close}
       [snackbar-content {:classes (select-keys classes [:root :message])
                          :message (reagent/as-element
                                    [:<>
                                     [error-icon {:class (:icon classes)}]
                                     (:msg @error)])
                          :action  (when (:retry-event @error)
                                     (reagent/as-element
                                      [button {:class    (:button classes)
                                               :on-click retry}
                                       "Retry"]))}]])))

(def snackbar ((with-styles styles) snackbar*))
