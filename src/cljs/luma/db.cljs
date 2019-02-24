(ns luma.db
  (:require [luma.util :refer [mobile?]]))

(def default-db
  {:selected-tags #{}
   :sort-key      :artist
   :sort-asc      true
   :text-search   ""
   :progress      0
   :mobile?       (mobile?)})
