(ns luma.handler
  (:require [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.reload :refer [wrap-reload]]
            [luma.routes :refer [routes]]))

(defn wrap-middleware [handler]
  (-> handler
    wrap-reload
    (wrap-defaults api-defaults)))

(def handler (wrap-middleware #'routes))
