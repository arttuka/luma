(ns luma.events
  (:require [clj-time.core :as time]
            [luma.websocket :as ws]
            [luma.db :as db]
            [luma.integration.spotify :as spotify]))

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
    (ws/send! uid [::set-spotify-id spotify-id])
    (let [user (db/get-account spotify-id)]
      (when (or (not (:last_loaded user))
                (time/after? (time/now) (time/plus (:last_loaded user) (time/hours 24))))
        (db/save-albums (:id user) (spotify/get-user-albums (:id user) (:access_token user) (:refresh_token user)))))
    (ws/send! uid [::albums (db/get-albums spotify-id)])))
