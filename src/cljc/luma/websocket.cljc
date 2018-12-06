(ns luma.websocket
  (:require [taoensso.sente :as sente]
            [taoensso.sente.packers.transit :as sente-transit]
            [taoensso.timbre :as log]
            [mount.core :refer [defstate]]
            #?@(:clj  [[taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
                       [compojure.core :refer [defroutes GET POST]]]
                :cljs [[goog.string :as gs]
                       goog.date.UtcDateTime])
            [cognitect.transit :as transit])
  #?(:clj (:import [org.joda.time])))

(def DateTime #?(:clj org.joda.time.DateTime, :cljs goog.date.UtcDateTime))

(defn read-date-time
  "Read RFC3339 string to DateTime."
  [s]
  #?(:clj  (org.joda.time.DateTime/parse s)
     :cljs (goog.date.UtcDateTime.fromIsoString s)))

(defn write-date-time
  "Represent DateTime in RFC3339 format string."
  [d]
  #?(:clj  (.toString (.withZone ^org.joda.time.DateTime d (org.joda.time.DateTimeZone/forID "UTC")))
     :cljs (str (.getUTCFullYear d)
                "-" (gs/padNumber (inc (.getUTCMonth d)) 2)
                "-" (gs/padNumber (.getUTCDate d) 2)
                "T" (gs/padNumber (.getUTCHours d) 2)
                ":" (gs/padNumber (.getUTCMinutes d) 2)
                ":" (gs/padNumber (.getUTCSeconds d) 2)
                "." (gs/padNumber (.getUTCMilliseconds d) 3)
                "Z")))

(def packer (sente-transit/->TransitPacker :json
                                           {:handlers {DateTime (transit/write-handler
                                                                  (constantly "datetime")
                                                                  write-date-time
                                                                  write-date-time)}}
                                           {:handlers {"datetime" read-date-time}}))

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
  ((:router r)))

(defstate router
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
