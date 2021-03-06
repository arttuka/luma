(ns luma.events
  (:require [config.core :refer [env]]
            [taoensso.timbre :as log]
            [luma.db :as db]
            [luma.websocket :as ws]
            [luma.integration.spotify :as spotify]
            [luma.integration.lastfm :as lastfm]
            [luma.util :refer [map-values older-than-1-month? when-let+]]))

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

(defn ^:private percentage [r]
  (int (* r 100)))

(defn load-tags [uid albums]
  (db/with-transaction
    (let [album-ids (into #{} (map :id) albums)
          artist-ids (into #{} (comp (mapcat :artists) (map :id)) albums)
          existing-albums (db/get-albums album-ids)
          existing-artists (db/get-artists artist-ids)
          new-albums (remove (comp existing-albums :id) albums)
          new-artists (sequence (comp (mapcat :artists) (remove (comp existing-artists :id)) (distinct)) albums)
          total (+ (count new-albums) (count new-artists))
          i (atom 0)
          report-progress! #(ws/send! uid [::progress (percentage (/ (swap! i inc) total))])
          album-tags (get-album-tags new-albums report-progress!)
          artist-tags (get-artist-tags new-artists report-progress!)]
      (db/save-tags! artist-tags album-tags)
      (db/get-tags album-ids))))

(defn get-and-send-tags [uid albums]
  (try
    (ws/send! uid [::tags (load-tags uid albums)])
    (catch Exception e
      (log/error e "Error while loading tags")
      (ws/send! uid [::error {:msg         "Unable to load tags. Try reloading the page later."
                              :retry-event ::retry-load-tags}]))))

(defn get-and-send-albums [uid spotify-access-token]
  (try
    (let [albums (spotify/get-user-albums spotify-access-token)]
      (ws/send! uid [::albums albums])
      (future (get-and-send-tags uid albums))
      (seq albums))
    (catch Exception e
      (log/error e "Error while loading albums")
      (ws/send! uid [::error {:msg         "Unable to load albums. Try reloading the page later."
                              :retry-event ::retry-load-tags}]))))

(defn send-playcounts [uid username albums]
  (try
    (let [existing-playcounts (db/with-transaction
                                (db/get-playcounts username))]
      (ws/send! uid [::playcounts (map-values existing-playcounts :playcount)])
      (doseq [album albums
              :let [{:keys [updated]} (get existing-playcounts (:id album))]
              :when (or (nil? updated)
                        (older-than-1-month? updated))
              :let [playcount (lastfm/get-album-playcount username (:name (first (:artists album))) (:title album))]]
        (ws/send! uid [::playcounts {(:id album) playcount}])
        (db/with-transaction
          (db/save-playcounts! username [{:album     (:id album)
                                          :playcount playcount}]))))
    (catch Exception e
      (log/error e "Error while loading playcounts")
      (ws/send! uid [::error {:msg "Unable to load playcounts. Try reloading the page later."}]))))

(defmethod ws/event-handler
  ::retry-load-tags
  [{:keys [uid ring-req]}]
  (when-let [spotify-user (get-in ring-req [:session :spotify-user])]
    (get-and-send-albums uid (:access_token spotify-user))))

(defmethod ws/event-handler
  ::ws/connect
  [{:keys [uid ring-req]}]
  (when-let+ [spotify-user (get-in ring-req [:session :spotify-user])
              albums (get-and-send-albums uid (:access_token spotify-user))
              lastfm-user (get-in ring-req [:session :lastfm-user])]
    (send-playcounts uid (:name lastfm-user) albums)))

(defmethod ws/event-handler
  ::erase-lastfm-data
  [{:keys [ring-req]}]
  (when-let [lastfm-user (get-in ring-req [:session :lastfm-user])]
    (db/with-transaction
      (db/erase-lastfm-data! (:name lastfm-user)))))
