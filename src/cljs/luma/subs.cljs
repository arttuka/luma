(ns luma.subs
  (:require [re-frame.core :as re-frame]
            [clojure.set :refer [intersection]]))

(re-frame/reg-sub
  ::uid
  (fn [db _]
    (:uid db)))

(re-frame/reg-sub
  ::spotify-id
  (fn [db _]
    (:spotify-id db)))

(re-frame/reg-sub
  ::albums
  (fn [db _]
    (:albums db)))

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
  ::filtered-albums
  :<- [::albums]
  :<- [::selected-tags]
  :<- [::tags-to-albums]
  (fn [[albums selected-tags tags-to-albums] [_]]
    (if (seq selected-tags)
      (->> (map tags-to-albums selected-tags)
           (reduce intersection)
           (map albums))
      (vals albums))))
