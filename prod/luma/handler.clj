(ns luma.handler
    (:require [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
              [luma.routes :refer [routes]]
              [luma.integration.spotify :refer [wrap-refresh-spotify]]))

(defn wrap-middleware [handler]
  (-> handler
    wrap-refresh-spotify
    (wrap-defaults (assoc-in site-defaults [:session :cookie-attrs :max-age] 2592000))))

(def handler (wrap-middleware #'routes))
