(ns user
  (:require [mount.core :as mount :refer [defstate]]
            [figwheel.main.api :as figwheel]
            [hawk.core :as hawk]
            [clojure.tools.namespace.repl :as repl]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [taoensso.timbre :as timbre]
            [luma.styles.main :as main-css]
            luma.main))

(timbre/swap-config! (fn [config] (assoc config :ns-whitelist ["luma.*"])))

(defn compile-garden-css! []
  (require 'luma.styles.main :reload)
  (let [f (io/file "./target/public/css/screen.css")]
    (io/make-parents f)
    (spit f main-css/css)))

(defstate figwheel
  :start (figwheel/start {:mode :serve} "dev")
  :stop (figwheel/stop "dev"))

(defstate garden-watcher
  :start (hawk/watch! [{:paths   ["src/clj/luma/styles"]
                        :handler (fn [ctx e]
                                   (when (and (= :modify (:kind e))
                                              (str/ends-with? (.getAbsolutePath (:file e)) ".clj"))
                                     (print "Garden CSS change recognized, recompiling ... ")
                                     (compile-garden-css!))
                                   ctx)}])
  :stop (hawk/stop! @garden-watcher))

(defn cljs-repl []
  (figwheel/cljs-repl "dev"))

(defn stop! []
  (mount/stop))

(defn start! []
  (compile-garden-css!)
  (mount/in-cljc-mode)
  (mount/start))

(defn restart! []
  (stop!)
  (repl/refresh :after 'user/start!))
