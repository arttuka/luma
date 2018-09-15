(ns luma.subs
  (:require [re-frame.core :as re-frame]
            [clojure.set :refer [intersection union]]
            [clojure.string :as str]))

(re-frame/reg-sub
  ::uid
  (fn [db _]
    (:uid db)))

(re-frame/reg-sub
  ::env
  (fn [db [_ k]]
    (get-in db [:env k])))

(re-frame/reg-sub
  ::spotify-id
  (fn [db _]
    (:spotify-id db)))

(re-frame/reg-sub
  ::albums
  (fn [db _]
    (:albums db)))

(re-frame/reg-sub
  ::progress
  (fn [db _]
    (:progress db)))

(re-frame/reg-sub
  ::tags-to-albums
  (fn [db _]
    (:tags-to-albums db)))

(re-frame/reg-sub
  ::tags
  (fn [db _]
    (:tags db)))

(re-frame/reg-sub
  ::subgenres
  (fn [db _]
    (:subgenres db)))

(re-frame/reg-sub
  ::all-tags
  :<- [::tags]
  :<- [::subgenres]
  (fn [[tags subgenres] _]
    (into tags (keys subgenres))))

(re-frame/reg-sub
  ::selected-tags
  (fn [db _]
    (:selected-tags db)))

(re-frame/reg-sub
  ::selected-tags-with-subgenres
  :<- [::subgenres]
  :<- [::selected-tags]
  (fn [[subgenres selected-tags] _]
    (into selected-tags (mapcat subgenres) selected-tags)))

(re-frame/reg-sub
  ::filtered-albums
  :<- [::albums]
  :<- [::selected-tags]
  :<- [::subgenres]
  :<- [::tags-to-albums]
  (fn [[albums selected-tags subgenres tags-to-albums] [_]]
    (if (seq selected-tags)
      (->> (for [tag selected-tags]
             (transduce (map tags-to-albums) union (tags-to-albums tag) (subgenres tag)))
           (reduce intersection)
           (map albums))
      (vals albums))))

(re-frame/reg-sub
  ::sort-key
  (fn [db _]
    (:sort-key db)))

(re-frame/reg-sub
  ::sort-asc
  (fn [db _]
    (:sort-asc db)))

(re-frame/reg-sub
  ::sorted-albums
  :<- [::filtered-albums]
  :<- [::sort-key]
  :<- [::sort-asc]
  (fn [[albums sort-key sort-asc] _]
    (let [sort-fn (case sort-key
                    :artist (comp str/lower-case :name first :artists)
                    :album (comp str/lower-case :title)
                    :added (comp str :added))
          sort-comp (if sort-asc
                      compare
                      (comp - compare))]
      (sort-by sort-fn sort-comp albums))))
