(ns luma.main
  (:require [mount.core :as mount]
            [taoensso.timbre :as timbre]
            luma.server
            luma.websocket
            luma.db)
  (:gen-class))

(defn -main []
  (timbre/merge-config! {:min-level :info})
  (mount/in-cljc-mode)
  (try
    (mount/start)
    (catch Throwable t
      (timbre/error t "Error while starting server")
      (mount/stop)
      (System/exit 1))))
