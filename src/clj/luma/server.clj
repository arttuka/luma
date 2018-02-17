(ns luma.server
  (:require [config.core :refer [env]]
            [org.httpkit.server :as http]
            [mount.core :refer [defstate]]
            [luma.handler :refer [handler]]
            luma.events))

(defstate server :start (http/run-server handler {:port (or (env :port) 8080)})
                 :stop (server))
