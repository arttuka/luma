(ns user
  (:require [aleph.http :as http]
            [clojure.tools.namespace.repl :as repl]
            [luma.handler :refer [dev-handler]]))

(defonce ^:private server (atom nil))

(defn stop! []
  (when-let [s @server]
    (.close s)))

(defn start! []
  (reset! server (http/start-server #'dev-handler {:port 8080})))

(defn restart! []
  (stop!)
  (repl/refresh :after 'user/start!))
