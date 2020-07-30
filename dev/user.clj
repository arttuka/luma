(ns user
  (:require [mount.core :as mount :refer [defstate]]
            [shadow.cljs.devtools.api :as cljs]
            [shadow.cljs.devtools.server :as shadow]
            [clojure.tools.namespace.repl :as repl]
            [taoensso.timbre :as timbre]
            luma.main))

(timbre/swap-config! (fn [config] (assoc config :ns-whitelist ["user" "luma.*"])))

(defstate shadow-cljs
  :start (do
           (shadow/start!)
           (cljs/watch :app))
  :stop (do
          (shadow/stop!)
          (cljs/stop-worker :app)))

(defn stop! []
  (mount/stop))

(defn start! []
  (mount/in-cljc-mode)
  (mount/start))

(defn restart! []
  (stop!)
  (repl/refresh :after 'user/start!))
