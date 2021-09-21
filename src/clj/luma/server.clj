(ns luma.server
  (:require [config.core :refer [env]]
            [aleph.http :as http]
            [mount.core :refer [defstate]]
            [luma.handler :refer [handler]]
            luma.events)
  (:import (java.io Closeable)))

(defstate ^{:on-reload :noop} server
  :start (http/start-server handler {:port (or (env :server-port) 8080)})
  :stop (.close ^Closeable @server))
