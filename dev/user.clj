(ns user
  (:require [mount.core :as mount :refer [defstate]]
            [figwheel.main.api :as figwheel]
            [clojure.tools.namespace.repl :as repl]
            [taoensso.timbre :as timbre]
            luma.main))

(timbre/swap-config! (fn [config] (assoc config :ns-whitelist ["user" "luma.*"])))

(defstate figwheel
  :start (figwheel/start {:mode :serve} "dev")
  :stop (figwheel/stop "dev"))

(defn cljs-repl []
  (figwheel/cljs-repl "dev"))

(defn stop! []
  (mount/stop))

(defn start! []
  (mount/in-cljc-mode)
  (mount/start))

(defn restart! []
  (stop!)
  (repl/refresh :after 'user/start!))
