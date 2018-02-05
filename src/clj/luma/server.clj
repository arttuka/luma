(ns luma.server
  (:require [luma.handler :refer [handler]]
            [config.core :refer [env]]
            [aleph.http :as http])
  (:gen-class))

 (defn -main [& args]
   (let [port (Integer/parseInt (or (env :port) "8080"))]
     (http/start-server handler {:port port})))
