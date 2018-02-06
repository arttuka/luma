(ns user
  (:require [org.httpkit.server :as http]
            [clojure.tools.namespace.repl :as repl]
            [luma.handler :refer [handler]]))

(defonce ^:private server (atom nil))

(defn stop! []
  (when-let [s @server]
    (s)))

(defn start! []
  (reset! server (http/run-server handler {:port 8080})))

(defn restart! []
  (stop!)
  (repl/refresh :after 'user/start!))
