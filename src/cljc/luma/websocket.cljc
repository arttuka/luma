(ns luma.websocket
  (:require [taoensso.sente :as sente]
            [taoensso.timbre :as log]
            [mount.core :refer [defstate]]
            #?(:clj [taoensso.sente.server-adapters.aleph :refer [get-sch-adapter]])
            #?(:clj [compojure.core :refer [defroutes GET POST]])
            #?(:clj [config.core :refer [env]])
            #?(:cljs [oops.core :refer [oget]])
            [luma.transit :as transit]))

(def path "/chsk")

(defn- init-ws []
  #?(:clj
     (let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn connected-uids]}
           (sente/make-channel-socket-server! (get-sch-adapter) {:packer transit/packer})]
       {:receive                     ch-recv
        :send!                       send-fn
        :connected-uids              connected-uids
        :ajax-post-fn                ajax-post-fn
        :ajax-get-or-ws-handshake-fn ajax-get-or-ws-handshake-fn})
     :cljs
     (let [{:keys [ch-recv send-fn chsk state]}
           (sente/make-channel-socket-client! path (oget js/window "csrf_token") {:packer transit/packer
                                                                                  :type   :auto})]
       {:receive ch-recv
        :send!   send-fn
        :state   state
        :chsk    chsk})))

(defmulti event-handler :id)

(defmethod event-handler :chsk/ws-ping [_])

#?(:clj
   (defmethod event-handler :default [{:keys [event ring-req]}]
     (log/debugf "Unhandled event %s from client %s" event (get-in ring-req [:session :uid]))))

#?(:cljs
   (defmethod event-handler :default [{:keys [event]}]
     (log/debugf "Unhandled event %s" event)))

(defn start-router []
  (let [conn (init-ws)]
    (assoc conn :router (sente/start-chsk-router! (:receive conn) event-handler))))

(defn stop-router [r]
  #?(:cljs (sente/chsk-disconnect! (:chsk r)))
  ((:router r)))

(defstate ^{:on-reload :noop} router
  :start (start-router)
  :stop (stop-router @router))

#?(:clj
   (defn send! [uid event]
     ((:send! @router) uid event))
   :cljs
   (defn send! [event]
     ((:send! @router) event)))

#?(:clj (defroutes routes
          (GET path request ((:ajax-get-or-ws-handshake-fn @router) request))
          (POST path request ((:ajax-post-fn @router) request))))
