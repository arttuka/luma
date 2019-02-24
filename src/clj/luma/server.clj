(ns luma.server
  (:require [config.core :refer [env]]
            [aleph.http :as http]
            [mount.core :refer [defstate]]
            [luma.handler :refer [handler]]
            luma.events))

(defstate server
  :start (http/start-server handler {:port (or (env :server-port) 8080)})
  :stop (.close @server))
