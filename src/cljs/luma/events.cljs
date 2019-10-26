(ns luma.events
  (:require [re-frame.core :as re-frame]
            [clojure.set :refer [union]]
            [luma.trie :refer [trie]]
            [luma.util :refer [map-by]]
            [luma.websocket :as ws]))

(re-frame/reg-fx
 ::ws/send
 (fn [event]
   (ws/send! event)))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   {:selected-tags #{}
    :sort-key      :artist
    :sort-asc      true
    :text-search   ""
    :progress      0}))

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
 ::set-lastfm-id
 (fn [db [_ lastfm-id]]
   (assoc db :lastfm-id lastfm-id)))

(defn ^:private add-to-albums [key albums vals]
  (reduce (fn [m [id val]]
            (if (contains? m id)
              (assoc-in m [id key] val)
              m))
          albums
          vals))

(def ^:private add-tags-to-albums (partial add-to-albums :tags))
(def ^:private add-playcounts-to-albums (partial add-to-albums :playcount))

(re-frame/reg-event-db
 ::albums
 (fn [db [_ albums]]
   (let [{:keys [playcounts albums-to-tags]} db
         albums (-> (map-by :id albums)
                    (add-tags-to-albums albums-to-tags)
                    (add-playcounts-to-albums playcounts))]
     (assoc db :albums albums))))

(re-frame/reg-event-db
 ::progress
 (fn [db [_ progress]]
   (assoc db :progress progress)))

(re-frame/reg-event-db
 ::tags
 (fn [db [_ tags]]
   (let [tags-to-albums (apply merge-with union (for [[id album-tags] tags
                                                      tag album-tags]
                                                  {tag #{id}}))]
     (assoc db
            :albums (add-tags-to-albums (:albums db) tags)
            :tags (into (trie) (mapcat val) tags)
            :albums-to-tags tags
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
 ::set-text-search
 (fn [db [_ value]]
   (assoc db :text-search value)))

(re-frame/reg-event-db
 ::sort-albums
 (fn [db [_ sort-key]]
   (assoc db :sort-key sort-key)))

(re-frame/reg-event-db
 ::change-sort-dir
 (fn [db _]
   (update db :sort-asc not)))

(re-frame/reg-event-db
 ::error
 (fn [db [_ error]]
   (assoc db :error error)))

(re-frame/reg-event-db
 ::close-error
 (fn [db _]
   (dissoc db :error)))

(re-frame/reg-event-fx
 ::ws/send
 (fn [_ [_ event]]
   {::ws/send event}))

(re-frame/reg-event-db
 ::playcounts
 (fn [db [_ playcounts]]
   (-> db
       (update :albums add-playcounts-to-albums playcounts)
       (assoc :playcounts playcounts))))
