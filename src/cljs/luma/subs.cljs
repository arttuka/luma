(ns luma.subs
  (:require [re-frame.core :as re-frame]
            [clojure.set :refer [intersection]]
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
 ::lastfm-id
 (fn [db _]
   (:lastfm-id db)))

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
 ::all-tags
 (fn [db _]
   (:tags db)))

(re-frame/reg-sub
 ::selected-tags
 (fn [db _]
   (:selected-tags db)))

(re-frame/reg-sub
 ::text-search
 (fn [db _]
   (:text-search db)))

(defn ^:private contains-string? [album s]
  (or (str/index-of (str/lower-case (:title album)) s)
      (some #(str/index-of (str/lower-case (:name %)) s) (:artists album))))

(re-frame/reg-sub
 ::filtered-albums
 :<- [::albums]
 :<- [::tags-to-albums]
 :<- [::selected-tags]
 :<- [::text-search]
 (fn [[albums tags-to-albums selected-tags text] [_]]
   (let [albums-from-tags (if (seq selected-tags)
                            (->> (map tags-to-albums selected-tags)
                                 (reduce intersection)
                                 (map albums))
                            (vals albums))]
     (if (str/blank? text)
       albums-from-tags
       (filter #(contains-string? % (str/lower-case text)) albums-from-tags)))))

(re-frame/reg-sub
 ::sort-key
 (fn [db _]
   (:sort-key db)))

(re-frame/reg-sub
 ::sort-asc
 (fn [db _]
   (:sort-asc db)))

(re-frame/reg-sub
 ::error
 (fn [db _]
   (:error db)))

(re-frame/reg-sub
 ::sorted-albums
 :<- [::filtered-albums]
 :<- [::sort-key]
 :<- [::sort-asc]
 (fn [[albums sort-key sort-asc] _]
   (let [sort-fn (case sort-key
                   :artist (comp str/lower-case :name first :artists)
                   :album (comp str/lower-case :title)
                   :added (comp str :added)
                   :playcount :playcount)
         sort-comp (if sort-asc
                     compare
                     (comp - compare))]
     (sort-by (juxt sort-fn :id) sort-comp albums))))
