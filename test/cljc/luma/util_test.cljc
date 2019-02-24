(ns luma.util-test
  (:require #?(:clj  [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest testing is]])
            [#?(:clj  clj-time.core
                :cljs cljs-time.core)
             :as time]
            #?(:clj  [luma.util :refer :all]
               :cljs [luma.util :refer [lazy-mapcat map-values map-by group-by-kv older-than-1-month?] :refer-macros [go-ex <?]])))

(deftest lazy-mapcat-test
  (testing "lazy-mapcat"
    (testing "works like mapcat"
      (is (= (mapcat range (range 10)) (lazy-mapcat range (range 10)))))
    (testing "is lazy"
      (let [hits (atom 0)
            s (lazy-mapcat (fn [_] (swap! hits inc) [1]) (range 10))]
        (is (zero? @hits))
        (is (= [1 1 1 1 1 1] (take 6 s)))
        (is (= 6 @hits))))))

#?(:clj (deftest ->hex-test
          (testing "->hex"
            (is (= "deadbeef" (->hex (byte-array [222 173 190 239])))))))

(deftest map-values-test
  (testing "map-values"
    (is (= {:foo 2, :bar 3}
           (map-values {:foo 1, :bar 2} inc)))))

(deftest map-by-test
  (testing "map-by"
    (testing "creates a map with (f item) as key and item as val"
      (is (= {2 1, 3 2, 4 3, 5 4}
             (map-by inc [1 2 3 4]))))
    (testing "uses the last item with the same key"
      (is (= {false 3, true 4}
             (map-by even? [1 2 3 4]))))
    (testing "maps the value with optional valfn"
      (is (= {2 0, 3 1, 4 2, 5 3}
             (map-by inc dec [1 2 3 4]))))))

(deftest group-by-kv-test
  (testing "group-by-kv"
    (is (= {true (range 1 1000 2)
            false (range 2 1001 2)}
           (group-by-kv even? inc (range 1000))))))

(deftest older-than-1-month-test
  (testing "older-than-1-month?"
    (is (not (older-than-1-month? (time/minus (time/now) (time/days 27)))))
    (is (older-than-1-month? (time/minus (time/now) (time/days 32))))))
