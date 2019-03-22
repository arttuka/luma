(ns luma.main
  (:require [mount.core :as mount]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.3rd-party.rolling :refer [rolling-appender]]
            luma.server
            luma.websocket
            luma.db)
  (:gen-class))

(def timbre-config {:level     :info
                    :appenders {:rolling (rolling-appender {:path "/var/log/luma/luma.log" :pattern :daily})}})

(defn -main []
  (timbre/merge-config! timbre-config)
  (mount/in-cljc-mode)
  (try
    (mount/start)
    (catch Throwable t
      (timbre/error t "Error while starting server")
      (mount/stop)
      (System/exit 1))))
