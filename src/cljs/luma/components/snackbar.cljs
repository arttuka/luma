(ns luma.components.snackbar
  (:require [reagent.core :as reagent :refer [atom with-let]]
            [re-frame.core :as re-frame]
            [reagent-mui.material.button :refer [button]]
            [reagent-mui.material.snackbar :refer [snackbar] :rename {snackbar mui-snackbar}]
            [reagent-mui.material.snackbar-content :refer [snackbar-content]]
            [reagent-mui.icons.error :refer [error] :rename {error error-icon}]
            [reagent-mui.styles :refer [styled]]
            [luma.events :as events]
            [luma.subs :as subs]
            [luma.util :as util]
            [luma.websocket :as ws]))

(def classes (util/make-classes
              "luma-snackbar"
              [:root
               :message
               :icon
               :button]))

(defn styles [{{:keys [palette spacing]} :theme}]
  (util/set-classes
   classes
   {:content-root {:background-color (get-in palette [:error :dark])}
    :message      {:display     :flex
                   :align-items :center}
    :icon         {:margin-right (spacing 1)}
    :button       {:color :white}}))

(defn snackbar* [{:keys [class-name]}]
  (with-let [error (re-frame/subscribe [::subs/error])
             close #(re-frame/dispatch [::events/close-error])
             retry #(do (re-frame/dispatch [::events/close-error])
                        (ws/send! [(:retry-event @error)]))]
    [mui-snackbar {:open     (boolean @error)
                   :on-close close
                   :class-name class-name}
     [snackbar-content {:classes {:root    (:content-root classes)
                                  :message (:message classes)}
                        :message (reagent/as-element
                                  [:<>
                                   [error-icon {:class (:icon classes)}]
                                   (:msg @error)])
                        :action  (when (:retry-event @error)
                                   (reagent/as-element
                                    [button {:class    (:button classes)
                                             :on-click retry}
                                     "Retry"]))}]]))

(def snackbar (styled snackbar* styles))
