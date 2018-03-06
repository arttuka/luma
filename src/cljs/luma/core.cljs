(ns luma.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [mount.core :as mount]
            [luma.events :as events]
            [luma.views :as views]
            [luma.config :as config]
            [luma.websocket :as ws]))


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount/start)
  (mount-root))

(defmethod ws/event-handler :chsk/state
  [{:keys [?data]}]
  (let [[_ new-data] ?data]
    (when (:first-open? new-data)
      (re-frame/dispatch [::ws/send [::ws/connect]])
      (re-frame/dispatch [::events/set-uid (:uid new-data)]))))

(defmethod ws/event-handler :chsk/recv
  [{:keys [?data send-fn]}]
  (let [[event data] ?data]
    (if (= event :chsk/ws-ping)
      (send-fn :chsk/ws-ping)
      (re-frame/dispatch [event data]))))
