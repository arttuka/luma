(ns luma.events
  (:require [luma.websocket :as ws]))

(defmethod ws/event-handler
  :chsk/uidport-open
  [_])

(defmethod ws/event-handler
  :chsk/uidport-close
  [_])

(defmethod ws/event-handler
  ::ws/connect
  [{:keys [uid ring-req] :as data}]
  (when-let [spotify-id (get-in ring-req [:session :spotify-id])]
    (ws/send! uid [::set-spotify-id spotify-id])))
