(ns handler
    (:require [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
              [luma.routes :refer [routes]]))

(defn wrap-middleware [handler]
  (-> handler
    (wrap-defaults api-defaults)))

(def handler (wrap-middleware #'routes))
