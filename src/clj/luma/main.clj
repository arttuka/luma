(ns luma.main
  (:require [mount.core :as mount]
            luma.server
            luma.websocket
            luma.db)
  (:gen-class))

(defn -main []
  (mount/in-cljc-mode)
  (mount/start))
