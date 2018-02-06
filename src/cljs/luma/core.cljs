(ns luma.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [mount.core :as mount]
            [luma.events :as events]
            [luma.routes :as routes]
            [luma.views :as views]
            [luma.config :as config]
            luma.websocket))


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (routes/app-routes)
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount/start)
  (mount-root))
