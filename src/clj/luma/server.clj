(ns luma.server
  (:require [config.core :refer [env]]
            [org.httpkit.server :as http]
            [mount.core :as mount :refer [defstate]]
            [luma.handler :refer [handler]])
  (:gen-class))

(defstate server :start (http/run-server handler {:port (or (env :port) 8080)})
                 :stop (server))

(defn -main []
  (mount/start))
