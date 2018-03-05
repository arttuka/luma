(ns user
  (:require [mount.core :as mount]
            [clojure.tools.namespace.repl :as repl]
            luma.main))

(defn stop! []
  (mount/stop))

(defn start! []
  (mount/in-cljc-mode)
  (mount/start))

(defn restart! []
  (stop!)
  (repl/refresh :after 'user/start!))
