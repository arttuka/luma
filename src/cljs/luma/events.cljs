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
  ::set-env
  (fn [db [_ env]]
    (assoc db :env env)))

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
    (assoc db :albums (into {} (for [album albums]
                                 [(:id album) album])))))

(re-frame/reg-event-db
  ::progress
  (fn [db [_ progress]]
    (assoc db :progress progress)))

(defn ^:private add-tags-to-albums [albums tags]
  (reduce (fn [albums [id tags]]
            (assoc-in albums [id :tags] (sort tags)))
          albums
          tags))

(re-frame/reg-event-db
  ::tags
  (fn [db [_ tags]]
    (let [tags-to-albums (apply merge-with union (for [[id album-tags] tags
                                                       tag album-tags]
                                                   {tag #{id}}))]
      (assoc db :albums (add-tags-to-albums (:albums db) tags)
                :tags (into (trie) (mapcat val) tags)
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
