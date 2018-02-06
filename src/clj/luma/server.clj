(ns luma.server
  (:require [config.core :refer [env]]
            [org.httpkit.server :as http]
            [luma.handler :refer [handler]])
  (:gen-class))

(defn -main [& args]
  (let [port (Integer/parseInt (or (env :port) "8080"))]
    (http/run-server handler {:port port})))
