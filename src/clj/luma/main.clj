(ns luma.main
  (:require [mount.core :as mount]
            luma.server
            luma.websocket)
  (:gen-class))

(defn -main []
  (mount/start))
