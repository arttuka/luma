(ns luma.core
  (:require [reagent.dom :as dom]
            [re-frame.core :as re-frame]
            [mount.core :as mount]
            [luma.app :refer [app]]
            [luma.events :as events]
            [luma.transit :as transit]
            [luma.websocket :as ws]))

(defn dev-setup []
  (when goog.DEBUG
    (enable-console-print!)
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (dom/render [app]
              (.getElementById js/document "app")))

(defn init []
  (re-frame/dispatch-sync [::events/initialize-db (transit/read js/initialDb)])
  (dev-setup)
  (mount/start)
  (mount-root))

(defmethod ws/event-handler :chsk/state
  [{:keys [?data]}]
  (let [[_ new-data] ?data]
    (when (:first-open? new-data)
      (ws/send! [::ws/connect]))))

(defmethod ws/event-handler :chsk/recv
  [{:keys [?data]}]
  (let [[event data] ?data]
    (when (not= :chsk/ws-ping event)
      (re-frame/dispatch [event data]))))

(init)
