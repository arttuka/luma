(ns luma.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
  ::uid
  (fn [db _]
    (:uid db)))

(re-frame/reg-sub
  ::spotify-id
  (fn [db _]
    (:spotify-id db)))
