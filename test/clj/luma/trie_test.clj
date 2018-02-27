(ns luma.trie-test
  (:require [clojure.test :refer :all]
            [luma.trie :refer :all]))

(deftest ilookup-test
  (testing "Trie implements clojure.lang.ILookup"
    (let [words ["foo" "foobar" "foobloo" "bar"]
          trie (make-trie words)]
      (testing "valAt"
        (testing "Finds contained words"
          (doseq [word words]
            (is (= word (get trie word)))))
        (testing "Doesn't find invalid words"
          (doseq [word ["fo" "foob" "trollface"]]
            (is (nil? (get trie word)))
            (is (= ::not-found (get trie word ::not-found)))))))))
