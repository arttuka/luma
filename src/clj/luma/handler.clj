(ns luma.handler
  (:require [config.core :refer [env]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.session.memory :refer [memory-store]]
            [luma.middleware.cache-control :refer [wrap-cache-control]]
            [luma.routes :refer [routes]]
            [luma.integration.spotify :refer [wrap-refresh-spotify]]))

(defonce ^:private dev-session-store (atom {}))

(defn wrap-dev-middleware [handler]
  (require 'ring.middleware.reload)
  (let [wrap-reload (ns-resolve 'ring.middleware.reload 'wrap-reload)]
    (-> handler
        wrap-reload
        wrap-refresh-spotify
        (wrap-defaults (-> site-defaults
                           (assoc-in [:session :cookie-attrs :max-age] 2592000)
                           (assoc-in [:session :cookie-attrs :same-site] :lax)
                           (assoc-in [:session :store] (memory-store dev-session-store)))))))

(defn wrap-prod-middleware [handler]
  (-> handler
      wrap-refresh-spotify
      (wrap-defaults (-> site-defaults
                         (update :security dissoc :frame-options :content-type-options)
                         (assoc-in [:session :cookie-attrs :max-age] 2592000)
                         (assoc-in [:session :cookie-attrs :same-site] :lax)))
      (wrap-cache-control {#"\.(css|js|png)$" "max-age=31536000"})))

(def handler (if (env :dev)
               (wrap-dev-middleware #'routes)
               (wrap-prod-middleware routes)))
