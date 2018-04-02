(ns luma.handler
  (:require [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.session.memory :refer [memory-store]]
            [luma.integration.spotify :refer [wrap-refresh-spotify]]
            [luma.routes :refer [routes]]))

(defonce ^:private session-store (atom {}))

(defn wrap-middleware [handler]
  (-> handler
    wrap-reload
    wrap-refresh-spotify
    (wrap-defaults (-> site-defaults
                     (assoc-in [:session :cookie-attrs :max-age] 2592000)
                     (assoc-in [:session :store] (memory-store session-store))))))

(def handler (wrap-middleware #'routes))
