(ns luma.subs-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [re-frame.core :as re-frame]
            [day8.re-frame.test :refer-macros [run-test-sync]]
            [luma.events :as events]
            [luma.subs :as subs]
            goog.date.UtcDateTime))

(def test-albums [{:id      "album-1"
                   :title   "Discovery"
                   :artists [{:id   "artist-1"
                              :name "Daft Punk"}]
                   :added   (goog.date.UtcDateTime.fromIsoString "2018-01-03")}
                  {:id      "album-2"
                   :title   "Random Access Memories"
                   :artists [{:id   "artist-1"
                              :name "Daft Punk"}]
                   :added   (goog.date.UtcDateTime.fromIsoString "2018-01-01")}
                  {:id      "album-3"
                   :title   "Dangerous Days"
                   :artists [{:id   "artist-3"
                              :name "Perturbator"}]
                   :added   (goog.date.UtcDateTime.fromIsoString "2018-01-02")}])

(def test-tags {"album-1" ["tag-1" "tag-3"]
                "album-2" ["tag-2" "tag-3"]
                "album-3" ["tag-2"]})

(defn init-db []
  (re-frame/dispatch [::events/initialize-db {}])
  (re-frame/dispatch [::events/albums test-albums])
  (re-frame/dispatch [::events/tags test-tags]))

(deftest filtered-albums-test
  (testing "filtered albums subscription"
    (testing "filter by tag"
      (run-test-sync
       (init-db)
       (let [sub (re-frame/subscribe [::subs/filtered-albums])
             albums #(into #{} (map :id @sub))]
         (is (= #{"album-1" "album-2" "album-3"} (albums)))
         (re-frame/dispatch [::events/select-tag "tag-3"])
         (is (= #{"album-1" "album-2"} (albums)))
         (re-frame/dispatch [::events/select-tag "tag-2"])
         (is (= #{"album-2"} (albums)))
         (re-frame/dispatch [::events/unselect-tag "tag-3"])
         (is (= #{"album-2" "album-3"} (albums))))))
    (testing "filter by text"
      (run-test-sync
       (init-db)
       (let [sub (re-frame/subscribe [::subs/filtered-albums])
             albums #(into #{} (map :id @sub))]
         (is (= #{"album-1" "album-2" "album-3"} (albums)))
         (re-frame/dispatch [::events/set-text-search "Disc"])
         (is (= #{"album-1"} (albums)))
         (re-frame/dispatch [::events/set-text-search "Daft"])
         (is (= #{"album-1" "album-2"} (albums)))
         (re-frame/dispatch [::events/set-text-search "Days"])
         (is (= #{"album-3"} (albums)))
         (re-frame/dispatch [::events/set-text-search ""])
         (is (= #{"album-1" "album-2" "album-3"} (albums))))))
    (testing "filter by both"
      (run-test-sync
       (init-db)
       (let [sub (re-frame/subscribe [::subs/filtered-albums])
             albums #(into #{} (map :id @sub))]
         (re-frame/dispatch [::events/set-text-search "Daft"])
         (re-frame/dispatch [::events/select-tag "tag-2"])
         (is (= #{"album-2"} (albums))))))))

(deftest sorted-albums-test
  (testing "sorted albums subscription"
    (run-test-sync
     (init-db)
     (let [sub (re-frame/subscribe [::subs/sorted-albums])
           albums #(map :id @sub)]
       (is (= ["album-1" "album-2" "album-3"] (albums)))
       (re-frame/dispatch [::events/sort-albums :added])
       (is (= ["album-2" "album-3" "album-1"] (albums)))
       (re-frame/dispatch [::events/change-sort-dir])
       (is (= ["album-1" "album-3" "album-2"] (albums)))
       (re-frame/dispatch [::events/sort-albums :album])
       (is (= ["album-2" "album-1" "album-3"] (albums)))
       (re-frame/dispatch [::events/select-tag "tag-3"])
       (is (= ["album-2" "album-1"] (albums)))))))
