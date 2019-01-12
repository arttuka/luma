(ns luma.events
  (:require [clojure.core.async :refer [go go-loop <! >! >!!] :as async]
            [config.core :refer [env]]
            [taoensso.timbre :as log]
            [luma.db :as db]
            [luma.websocket :as ws]
            [luma.integration.spotify :as spotify]
            [luma.integration.lastfm :as lastfm]
            [luma.util :refer [go-ex <? map-values older-than-1-month?]]))

(defmethod ws/event-handler
  :chsk/uidport-open
  [_])

(defmethod ws/event-handler
  :chsk/uidport-close
  [_])

(defn get-album-tags [albums report-progress!]
  (doall (map (fn [album]
                (report-progress!)
                (let [album-tags (into #{} (mapcat #(lastfm/get-album-tags (:name %) (:title album))) (:artists album))]
                  {:id      (:id album)
                   :tags    album-tags
                   :artists (map :id (:artists album))}))
              albums)))

(defn get-artist-tags [artists report-progress!]
  (doall (map (fn [artist]
                (report-progress!)
                {:id   (:id artist)
                 :tags (lastfm/get-artist-tags (:name artist))})
              artists)))

(defn load-tags [albums progress-ch]
  (db/with-transaction
    (let [album-ids (into #{} (map :id) albums)
          artist-ids (into #{} (comp (mapcat :artists) (map :id)) albums)
          existing-albums (db/get-albums db/*tx* album-ids)
          existing-artists (db/get-artists db/*tx* artist-ids)
          new-albums (remove (comp existing-albums :id) albums)
          new-artists (sequence (comp (mapcat :artists) (remove (comp existing-artists :id)) (distinct)) albums)
          total (+ (count new-albums) (count new-artists))
          i (atom 0)
          report-progress! #(>!! progress-ch (/ (swap! i inc) total))
          album-tags (get-album-tags new-albums report-progress!)
          artist-tags (get-artist-tags new-artists report-progress!)]
      (db/save-tags! db/*tx* artist-tags album-tags)
      (db/get-tags db/*tx* album-ids))))

(defn send-albums [uid spotify-access-token]
  (let [albums (spotify/get-user-albums spotify-access-token)
        progress-ch (async/chan 10)
        tags-ch (go-ex (load-tags albums progress-ch))]
    (ws/send! uid [::albums albums])
    (go-loop [i (<! progress-ch)]
      (when i
        (ws/send! uid [::progress (int (* i 100))])
        (recur (<! progress-ch))))
    (go
      (try
        (ws/send! uid [::tags (<? tags-ch)])
        (catch Exception e
          (log/error e "Error while loading tags")
          (async/close! progress-ch)
          (ws/send! uid [::error {:msg         "Unable to load tags. Try reloading the page later."
                                  :retry-event ::retry-load-tags}]))))
    albums))

(defn send-playcounts [uid username albums]
  (try
    (let [existing-playcounts (db/with-transaction
                                (db/get-playcounts db/*tx* username))]
      (ws/send! uid [::playcounts (map-values existing-playcounts :playcount)])
      (doseq [album albums
              :let [{:keys [updated]} (get existing-playcounts (:id album))]
              :when (or (nil? updated)
                        (older-than-1-month? updated))
              :let [playcount (lastfm/get-album-playcount username (:name (first (:artists album))) (:title album))]]
        (ws/send! uid [::playcounts {(:id album) playcount}])
        (db/with-transaction
          (db/save-playcounts! db/*tx* username [{:album     (:id album)
                                                  :playcount playcount}]))))
    (catch Exception e
      (log/error e "Error while loading playcounts"))))

(defmethod ws/event-handler
  ::retry-load-tags
  [{:keys [uid ring-req]}]
  (when-let [spotify-user (get-in ring-req [:session :spotify-user])]
    (send-albums uid (:access_token spotify-user))))

(defmethod ws/event-handler
  ::ws/connect
  [{:keys [uid ring-req]}]
  (ws/send! uid [::set-env {:spotify-client-id    (env :spotify-client-id)
                            :spotify-redirect-uri (str (env :baseurl) "/spotify-callback")
                            :lastfm-api-key       (env :lastfm-api-key)
                            :lastfm-redirect-uri  (str (env :baseurl) "/lastfm-callback")}])
  (let [spotify-user (get-in ring-req [:session :spotify-user])
        lastfm-user (get-in ring-req [:session :lastfm-user])]
    (when lastfm-user (ws/send! uid [::set-lastfm-id (:name lastfm-user)]))
    (let [albums (when spotify-user
                   (ws/send! uid [::set-spotify-id (:id spotify-user)])
                   (send-albums uid (:access_token spotify-user)))]
      (when (and lastfm-user albums)
        (go (send-playcounts uid (:name lastfm-user) albums))))))
