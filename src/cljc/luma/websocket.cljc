(ns luma.websocket
  (:require [taoensso.sente :as sente]
            [taoensso.sente.packers.transit :as sente-transit]
            [taoensso.timbre :as log]
            [mount.core :refer [defstate]]
    #?@(:clj [
            [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
            [compojure.core :refer [defroutes GET POST]]])))

(def packer (sente-transit/->TransitPacker :json
                                           {:handlers {}}
                                           {:handlers {}}))

(def path "/chsk")

(defn- init-ws []
  #?(:clj
     (let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn connected-uids]}
           (sente/make-channel-socket! (get-sch-adapter) {:packer packer})]
       {:receive                     ch-recv
        :send!                       send-fn
        :connected-uids              connected-uids
        :ajax-post-fn                ajax-post-fn
        :ajax-get-or-ws-handshake-fn ajax-get-or-ws-handshake-fn})
     :cljs
     (let [{:keys [ch-recv send-fn chsk state]} (sente/make-channel-socket! path {:packer packer
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
  ((:router #?(:clj r, :cljs @r))))

(defstate router
  :start (start-router)
  :stop (stop-router router))


#?(:clj
   (defn send! [uid event]
     ((:send! router) uid event))
   :cljs
   (defn send! [event]
     ((:send! @router) event)))


#?(:clj (defroutes routes
          (GET path request ((:ajax-get-or-ws-handshake-fn router) request))
          (POST path request ((:ajax-post-fn router) request))))
