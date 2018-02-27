(ns luma.trie-test
  (:require [clojure.test :refer :all]
            [luma.trie :refer :all])
  (:import (luma.trie Trie)))

(deftest ilookup-test
  (testing "Trie implements clojure.lang.ILookup"
    (let [words ["bar" "foo" "foobar" "foobloo"]
          trie (make-trie words)]
      (testing "valAt"
        (testing "Finds contained words"
          (doseq [word words]
            (is (= word (get trie word)))))
        (testing "Doesn't find invalid words"
          (doseq [word ["fo" "foob" "trollface"]]
            (is (nil? (get trie word)))
            (is (= ::not-found (get trie word ::not-found)))))))))

(deftest ipersistentcollection-test
  (testing "Trie implements clojure.lang.IPersistentCollection"
    (let [words ["bar" "foo" "foobar" "foobloo"]
          trie (make-trie words)]
      (testing "contains"
        (testing "finds contained words"
          (doseq [word words]
            (is (contains? trie word))))
        (testing "doesn't find invalid words"
          (doseq [word ["fo" "foob" "trollface"]]
            (is (not (contains? trie word))))))
      (testing "disjoin"
        (let [new-trie (disj trie "foo")]
          (testing "returns a Trie"
            (is (instance? Trie new-trie)))
          (testing "removes disjoined word"
            (is (not (contains? new-trie "foo"))))
          (testing "doesn't affect other words with same prefix"
            (is (contains? trie "foobar"))
            (is (contains? trie "foobloo")))))
      (testing "cons"
        (let [new-trie (conj trie "foob" "zing")]
          (testing "returns a Trie"
            (is (instance? Trie new-trie)))
          (testing "adds words to trie"
            (is (contains? new-trie "foob"))
            (is (contains? new-trie "zing")))
          (testing "doesn't remove any words"
            (doseq [word words]
              (is (contains? new-trie word))))))
      (testing "count"
        (testing "returns count of elements in the trie"
          (is (= 4 (count trie)))))
      (testing "empty"
        (let [empty-trie (empty trie)]
          (testing "returns an empty trie"
            (is (instance? Trie empty-trie))
            (is (zero? (count empty-trie))))))
      (testing "equiv"
        (let [other-trie (make-trie ["bar" "foo" "foobar"])]
          (testing "compares tries based on their contents"
            (is (not= trie other-trie))
            (is (= trie (conj other-trie "foobloo")))
            (is (not= trie (conj other-trie "foobloo" "trollface"))))))
      (testing "seq"
        (testing "returns contained strings in alphabetical order"
          (is (= words (seq trie))))))))

(deftest persistent-test
  (let [words ["bar" "foo" "foobar" "foobloo"]
        trie (make-trie words)]
    (testing "conj doesn't change the trie"
      (let [new-trie (conj trie "foob")]
        (is (= ["bar" "foo" "foob" "foobar" "foobloo"] (seq new-trie)))
        (is (= words (seq trie)))))
    (testing "disj doesn't change the trie"
      (let [new-trie (disj trie "foo")]
        (is (= ["bar" "foobar" "foobloo"] (seq new-trie)))
        (is (= words (seq trie)))))))
