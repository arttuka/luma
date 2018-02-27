(ns luma.util-test
  (:require [clojure.test :refer :all]
            [luma.util :refer :all]))

(deftest when-let+-test
  (testing "when-let+"
    (testing "works with any number of bindings: "
      (testing "0"
        (is (= :foo (when-let+ []
                      :foo))))
      (testing "1"
        (is (= 4 (when-let+ [a (+ 2 2)]
                   a))))
      (testing "more"
        (is (= 4 (when-let+ [a 1
                             b (+ 3 a)]
                   b)))))
    (testing "short-circuits to nil if any binding is nil"
      (is (nil? (when-let+ [a nil]
                  (throw (Exception.)))))
      (is (nil? (when-let+ [a 1
                            b nil
                            c (throw (Exception.))]))))))

(deftest grouping-test
  (testing "grouping"
    (is (= (set [{:foo 1, :bars [{:bar 1} {:bar 3} {:bar 5}]}
                 {:foo 2, :bars [{:bar 2} {:bar 4} {:bar 6}]}])
           (set (grouping [:foo] :bars [{:foo 1, :bar 1}
                                        {:foo 2, :bar 2}
                                        {:foo 1, :bar 3}
                                        {:foo 2, :bar 4}
                                        {:foo 1, :bar 5}
                                        {:foo 2, :bar 6}]))))))

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
