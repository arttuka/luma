(ns ^:figwheel-hooks luma.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [mount.core :as mount]
            [oops.core :refer [oget]]
            [luma.app :refer [app]]
            [luma.events :as events]
            [luma.transit :as transit]
            [luma.websocket :as ws]))

(defn dev-setup []
  (when goog.DEBUG
    (enable-console-print!)
    (println "dev mode")))

(defn ^:after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [app]
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
