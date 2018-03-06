(ns user
  (:require [mount.core :as mount :refer [defstate]]
            [figwheel-sidecar.repl-api :as figwheel]
            [hawk.core :as hawk]
            [clojure.tools.namespace.repl :as repl]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [luma.styles.main :as main-css]
            luma.main))

(defn compile-garden-css! []
  (require 'luma.styles.main :reload)
  (let [f (io/file "./resources/public/css/screen.css")]
    (io/make-parents f)
    (spit f main-css/css)))

(defstate figwheel
  :start (figwheel/start-figwheel!)
  :stop (figwheel/stop-figwheel!))

(defstate figwheel-autobuilder
  :start (figwheel/start-autobuild "dev")
  :stop (figwheel/stop-autobuild "dev"))

(defstate garden-watcher
  :start (do
           (hawk/watch! [{:paths   ["src/clj/luma/styles"]
                          :handler (fn [ctx e]
                                     (when (and (= :modify (:kind e))
                                                (str/ends-with? (.getAbsolutePath (:file e)) ".clj"))
                                       (print "Garden CSS change recognized, recompiling ... ")
                                       (compile-garden-css!))
                                     ctx)}]))
  :stop (hawk/stop! @garden-watcher))

(defn stop! []
  (mount/stop))

(defn start! []
  (mount/in-cljc-mode)
  (mount/start))

(defn restart! []
  (stop!)
  (repl/refresh :after 'user/start!))
