(ns luma.trie-test
  (:require #?(:clj  [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest testing is]])
            [luma.trie :refer [trie search #?(:cljs Trie)]])
  #?(:clj (:import (luma.trie Trie))))

(deftest ilookup-test
  (testing "Trie implements clojure.lang.ILookup"
    (let [words ["bar" "foo" "foobar" "foobloo"]
          t (trie words)]
      (testing "valAt"
        (testing "Finds contained words"
          (doseq [word words]
            (is (= word (get t word)))))
        (testing "Doesn't find invalid words"
          (doseq [word ["fo" "foob" "trollface"]]
            (is (nil? (get t word)))
            (is (= ::not-found (get t word ::not-found)))))))))

(deftest ipersistentcollection-test
  (testing "Trie implements clojure.lang.IPersistentCollection"
    (let [words ["bar" "foo" "foobar" "foobloo"]
          t (trie words)]
      (testing "contains"
        (testing "finds contained words"
          (doseq [word words]
            (is (contains? t word))))
        (testing "doesn't find invalid words"
          (doseq [word ["fo" "foob" "trollface"]]
            (is (not (contains? t word))))))
      (testing "disjoin"
        (let [new-trie (disj t "foo")]
          (testing "returns a Trie"
            (is (instance? Trie new-trie)))
          (testing "removes disjoined word"
            (is (not (contains? new-trie "foo"))))
          (testing "doesn't affect other words with same prefix"
            (is (contains? t "foobar"))
            (is (contains? t "foobloo")))))
      (testing "cons"
        (let [new-trie (conj t "foob" "zing")]
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
          (is (= 4 (count t)))))
      (testing "empty"
        (let [empty-trie (empty t)]
          (testing "returns an empty trie"
            (is (instance? Trie empty-trie))
            (is (zero? (count empty-trie))))))
      (testing "equiv"
        (let [other-trie (trie ["bar" "foo" "foobar"])]
          (testing "compares tries based on their contents"
            (is (not= t other-trie))
            (is (= t (conj other-trie "foobloo")))
            (is (not= t (conj other-trie "foobloo" "trollface"))))))
      (testing "seq"
        (testing "returns contained strings in alphabetical order"
          (is (= words (seq t))))
        (testing "returns nil for empty trie"
          (is (nil? (seq (trie)))))))))

(deftest itrie-test
  (testing "Trie implements luma.trie.ITrie"
    (let [words ["bar" "foo" "foobar" "foobloo"]
          t (trie words)]
      (testing "search"
        (testing "returns matching words in alphabetical order"
          (is (= ["foo" "foobar" "foobloo"] (search t "foo"))))
        (testing "returns empty sequence if nothing is found"
          (is (empty? (search t "trollface"))))))))

(deftest ifn-test
  (testing "Trie implements IFn"
    (let [words ["bar" "foo" "foobar" "foobloo"]
          t (trie words)]
      (testing "search"
        (testing "returns matching words in alphabetical order"
          (is (= ["foo" "foobar" "foobloo"] (t "foo"))))
        (testing "returns empty sequence if nothing is found"
          (is (empty? (t "trollface"))))))))

(deftest persistent-test
  (let [words ["bar" "foo" "foobar" "foobloo"]
        t (trie words)]
    (testing "conj doesn't change the trie"
      (let [new-trie (conj t "foob")]
        (is (= ["bar" "foo" "foob" "foobar" "foobloo"] (seq new-trie)))
        (is (= words (seq t)))))
    (testing "disj doesn't change the trie"
      (let [new-trie (disj t "foo")]
        (is (= ["bar" "foobar" "foobloo"] (seq new-trie)))
        (is (= words (seq t)))))))

(deftest meta-test
  (let [words ["bar" "foo" "foobar" "foobloo"]
        t (trie words)]
    (testing "metadata can be attached"
      (is (= {:foo true}
             (meta (with-meta t {:foo true})))))
    (testing "attaching metadata doesn't affect contains"
      (is (= words
             (seq (with-meta t {:foo true})))))
    (testing "conj preserves metadata"
      (is (= {:foo true}
             (-> t
                 (with-meta {:foo true})
                 (conj "foob")
                 (meta)))))
    (testing "disj preserves metadata"
      (is (= {:foo true}
             (-> t
                 (with-meta {:foo true})
                 (disj "foo")
                 (meta)))))))
