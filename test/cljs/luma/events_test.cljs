(ns luma.events-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [re-frame.core :as re-frame]
            [day8.re-frame.test :refer-macros [run-test-sync]]
            [luma.events :as events]
            [luma.subs :as subs]))

(def test-albums [{:id "id-1", :name "name-1"}
                  {:id "id-2", :name "name-2"}])

(def test-tags {"id-1" ["tag-1" "tag-3"]
                "id-2" ["tag-2" "tag-3"]})

(deftest albums-test
  (testing "albums event"
    (run-test-sync
     (let [albums-sub (re-frame/subscribe [::subs/albums])
           expected-albums {"id-1" {:id "id-1", :name "name-1"}
                            "id-2" {:id "id-2", :name "name-2"}}]
       (is (nil? @albums-sub))
       (re-frame/dispatch [::events/albums test-albums])
       (is (= expected-albums @albums-sub))))))

(deftest tags-test
  (testing "tags event"
    (run-test-sync
     (let [albums-sub (re-frame/subscribe [::subs/albums])
           tags-sub (re-frame/subscribe [::subs/all-tags])
           tags-to-albums-sub (re-frame/subscribe [::subs/tags-to-albums])
           expected-tags-to-albums {"tag-1" #{"id-1"}
                                    "tag-2" #{"id-2"}
                                    "tag-3" #{"id-1" "id-2"}}]
       (is (nil? @tags-sub))
       (is (nil? @tags-to-albums-sub))
       (re-frame/dispatch [::events/albums test-albums])
       (re-frame/dispatch [::events/tags test-tags])
       (is (= #{"tag-1" "tag-2" "tag-3"} (into #{} @tags-sub)))
       (is (= (test-tags "id-1") (get-in @albums-sub ["id-1" :tags])))
       (is (= (test-tags "id-2") (get-in @albums-sub ["id-2" :tags])))
       (is (= expected-tags-to-albums @tags-to-albums-sub))))))
