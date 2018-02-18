(ns luma.events
  (:require [clj-time.core :as time]
            [luma.websocket :as ws]
            [luma.db :as db]
            [luma.integration.spotify :as spotify]
            [luma.integration.lastfm :as lastfm]))

(defmethod ws/event-handler
  :chsk/uidport-open
  [_])

(defmethod ws/event-handler
  :chsk/uidport-close
  [_])

(defn get-and-save-spotify-albums [user]
  (db/save-albums (:id user) (spotify/get-user-albums (:id user) (:access_token user) (:refresh_token user))))

(defn get-and-save-lastfm-tags [user]
  (db/with-transaction
    (let [albums (db/get-albums user)
          artists (into #{} (mapcat :artists albums))]
      (doseq [album albums
              :let [tags (into #{} (mapcat #(lastfm/get-album-tags (:name %) (:title album)) (:artists album)))]]
        (db/save-album-tags (:id album) tags))
      (doseq [artist artists
              :let [tags (lastfm/get-artist-tags (:name artist))]]
        (db/save-artist-tags (:artist_id artist) tags)))))

(defmethod ws/event-handler
  ::ws/connect
  [{:keys [uid ring-req] :as data}]
  (when-let [spotify-id (get-in ring-req [:session :spotify-id])]
    (ws/send! uid [::set-spotify-id spotify-id])
    (ws/send! uid [::albums (db/get-albums spotify-id)])
    (let [user (db/get-account spotify-id)]
      (when (or (not (:last_loaded user))
                (time/after? (time/now) (time/plus (:last_loaded user) (time/hours 24))))
        (get-and-save-spotify-albums user)
        (get-and-save-lastfm-tags user)
        (ws/send! uid [::albums (db/get-albums spotify-id)])))))
