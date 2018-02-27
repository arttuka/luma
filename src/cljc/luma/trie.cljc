(ns luma.trie
  (:require [clojure.string :as str])
  (:import (clojure.lang ILookup IPersistentSet)))

(defprotocol ITrie
  (search [trie s] "Find all strings that start with s"))

#?(:clj
   (deftype Trie [value contains children]
     ILookup
     (valAt [this k]
       (.valAt this k nil))
     (valAt [this k not-found]
       (let [[c & cs] k]
         (cond
           (and (not c) contains) value
           (and c (contains? children c)) (.valAt (get children c) cs not-found)
           :else not-found)))

     IPersistentSet
     (disjoin [this key]
       (let [[c & cs] key]
         (if (not c)
           (Trie. value false children)
           (Trie. value contains (update children c disj cs)))))
     (contains [this key]
       (let [not-found (Object.)]
         (not= not-found (get this key not-found))))
     (get [this key]
       (.valAt this key))
     (count [this]
       (count (seq this)))
     (cons [this o]
       (let [[c & cs] o]
         (cond
           (not c) (Trie. value true children)
           (contains? children c) (Trie. value contains (update children c conj cs))
           :else (Trie. value contains (assoc children c (conj (Trie. (str value c) false {}) cs))))))
     (empty [this]
       (Trie. "" false {}))
     (equiv [this o]
       (= (seq this) (seq o)))
     (seq [self]
       (let [ks (sort (keys children))
             subseq (mapcat #(seq (get children %)) ks)]
         (if contains
           (cons value subseq)
           subseq)))

     ITrie
     (search [this s]
       (let [[c & cs] s]
         (cond
           (not c) (seq this)
           (contains? children c) (search (get children c) cs)
           :else [])))))

(defn make-trie [strs]
  (into (Trie. "" false {}) (set (map str/lower-case strs))))
