(ns luma.events
  (:require [re-frame.core :as re-frame]
            [luma.db :as db]
            [luma.websocket :as ws]))

(re-frame/reg-fx
  ::ws/send
  (fn [event]
    (ws/send! event)))

(re-frame/reg-event-db
 ::initialize-db
 (fn  [_ _]
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
    (assoc db :albums albums)))

(re-frame/reg-event-fx
  ::ws/send
  (fn [_ [_ event]]
    {::ws/send event}))
