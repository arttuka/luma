(ns luma.handler
    (:require [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
              [luma.routes :refer [routes]]))

(defn wrap-middleware [handler]
  (-> handler
    (wrap-defaults (assoc-in site-defaults [:session :cookie-attrs :max-age] 2592000))))

(def handler (wrap-middleware #'routes))
