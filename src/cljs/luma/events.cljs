(ns luma.events
  (:require [re-frame.core :as re-frame]
            [clojure.set :refer [union]]
            [luma.db :as db]
            [luma.trie :refer [trie]]
            [luma.websocket :as ws]))

(re-frame/reg-fx
  ::ws/send
  (fn [event]
    (ws/send! event)))

(re-frame/reg-event-db
  ::initialize-db
  (fn [_ _]
    db/default-db))

(re-frame/reg-event-db
  ::set-uid
  (fn [db [_ uid]]
    (assoc db :uid uid)))

(re-frame/reg-event-db
  ::set-spotify-id
  (fn [db [_ spotify-id]]
    (assoc db :spotify-id spotify-id)))

(re-frame/reg-event-db
  ::albums
  (fn [db [_ albums]]
    (let [tags (into (trie) (mapcat :tags) albums)
          tags-to-albums (apply merge-with union (for [album albums
                                                       tag (:tags album)]
                                                   {tag #{(:id album)}}))
          albums (into {} (for [album albums]
                            [(:id album) album]))]
      (assoc db :tags tags
                :albums albums
                :tags-to-albums tags-to-albums))))

(re-frame/reg-event-db
  ::select-tag
  (fn [db [_ tag]]
    (if (contains? (:tags db) tag)
      (update db :selected-tags conj tag)
      db)))

(re-frame/reg-event-db
  ::unselect-tag
  (fn [db [_ tag]]
    (update db :selected-tags disj tag)))

(re-frame/reg-event-db
  ::sort-albums
  (fn [db [_ sort-key]]
    (assoc db :sort-key sort-key)))

(re-frame/reg-event-db
  ::change-sort-dir
  (fn [db _]
    (update db :sort-asc not)))

(re-frame/reg-event-fx
  ::ws/send
  (fn [_ [_ event]]
    {::ws/send event}))
