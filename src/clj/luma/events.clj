(ns luma.events
  (:require [clojure.core.async :refer [go go-loop <! >! chan]]
            [config.core :refer [env]]
            [luma.db :as db]
            [luma.datomic :as datomic]
            [luma.util :refer [map-by]]
            [luma.websocket :as ws]
            [luma.integration.spotify :as spotify]
            [luma.integration.lastfm :as lastfm]))

(defmethod ws/event-handler
  :chsk/uidport-open
  [_])

(defmethod ws/event-handler
  :chsk/uidport-close
  [_])

(defn get-and-save-artist-tags [artist]
  (let [tags (lastfm/get-artist-tags artist)]
    (db/save-artist-tags artist tags)
    tags))

(defn get-and-save-album-tags [artist title]
  (let [tags (lastfm/get-album-tags artist title)]
    (db/save-album-tags artist title tags)
    tags))

(defn get-and-save-tags [artists title]
  (let [album-tags (into #{} (mapcat #(get-and-save-album-tags % title) artists))
        tags (into album-tags (mapcat get-and-save-artist-tags artists))]
    tags))

(defn get-album-tags [artists title]
  (db/with-transaction
    (if (db/has-tags? artists title)
      (db/get-tags artists title)
      (get-and-save-tags artists title))))

(defn load-album-tags [album]
  (let [album-tags (into #{} (mapcat #(lastfm/get-album-tags (:name %) (:title album)) (:artists album)))
        artist-tags (map-by :id (comp lastfm/get-artist-tags :name) (:artists album))]
    [album-tags artist-tags]))

(defn load-missing-tags [albums progress-ch]
  (let [album-exists? (datomic/get-existing-albums (datomic/get-db) (map :id albums))]
    (go-loop [[album & albums] albums
              tags {:albums  {}
                    :artists {}}
              i 0]
      (>! progress-ch i)
      (cond
        (nil? album) tags
        (album-exists? (:id album)) (recur albums tags (inc i))
        :else (let [[album-tags artist-tags] (load-album-tags album)
                    new-tags (-> tags
                               (assoc-in [:albums (:id album)] {:tags    album-tags
                                                                :artists (map :id (:artists album))})
                               (update :artists into artist-tags))]
                (recur albums new-tags (inc i)))))))

(defn load-tags [albums progress-ch]
  (go-loop [[album & albums] albums
            album-tags (transient {})
            i 0]
    (if-not album
      (persistent! album-tags)
      (let [tags (get-album-tags (map :name (:artists album)) (:title album))]
        (>! progress-ch i)
        (recur albums (assoc! album-tags (:id album) tags) (inc i))))))

(defmethod ws/event-handler
  ::ws/connect
  [{:keys [uid ring-req]}]
  (ws/send! uid [::set-env {:spotify-client-id    (env :spotify-client-id)
                            :spotify-redirect-uri (str (env :baseurl) "/spotify-callback")}])
  (when-let [spotify-user (get-in ring-req [:session :spotify-user])]
    (ws/send! uid [::set-spotify-id (:id spotify-user)])
    (let [all-albums (spotify/get-user-albums (:access_token spotify-user))
          progress-ch (chan 10)]
      (ws/send! uid [::albums all-albums])
      (go-loop [i (<! progress-ch)]
        (when i
          (ws/send! uid [::progress i])
          (recur (<! progress-ch))))
      (go
        (let [{:keys [albums artists]} (<! (load-missing-tags all-albums progress-ch))
              db (datomic/save-albums! artists albums)]
          (ws/send! uid [::tags (datomic/get-album-genres db (map :id all-albums))]))))))
