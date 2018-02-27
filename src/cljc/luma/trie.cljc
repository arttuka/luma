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
     (disjoin [this key])
     (contains [this key])
     (get [this key])
     (count [this])
     (cons [this o]
       (let [[c & cs] o]
         (cond
           (not c) (Trie. value true children)
           (contains? children c) (Trie. value contains (update children c conj cs))
           :else (Trie. value contains (assoc children c (conj (Trie. (str value c) false {}) cs))))))
     (empty [this])
     (equiv [self o])
     (seq [self])

     ITrie
     (search [this s])))

(def empty (Trie. "" false {}))

(defn make-trie [strs]
  (into empty (set (map str/lower-case strs))))
