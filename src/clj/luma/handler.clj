(ns luma.handler
  (:require [config.core :refer [env]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.session.memory :refer [memory-store]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [luma.middleware.etag :refer [wrap-etag]]
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
                         (assoc-in [:session :cookie-attrs :max-age] 2592000)
                         (assoc-in [:session :cookie-attrs :same-site] :lax)))
      (wrap-etag {:paths [#".*\.(css|png|js)$"]})
      wrap-not-modified))

(def handler (if (env :dev)
               (wrap-dev-middleware #'routes)
               (wrap-prod-middleware routes)))
