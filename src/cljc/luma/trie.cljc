(ns luma.trie
  (:require [clojure.string :as str])
  (:import (clojure.lang ILookup IPersistentSet)))

(defprotocol ITrie
  (search [trie s] "Find all strings that start with s"))

#?(:clj
   (deftype Trie [contains children]
     ILookup
     (valAt [this k])
     (valAt [this k not-found])

     IPersistentSet
     (disjoin [this key])
     (contains [this key])
     (get [this key])
     (count [this])
     (cons [this o])
     (empty [this])
     (equiv [self o])
     (seq [self])

     ITrie
     (search [this s])))

(def empty (Trie. false {}))

(defn make-trie [strs]
  (into empty (set (map str/lower-case strs))))
