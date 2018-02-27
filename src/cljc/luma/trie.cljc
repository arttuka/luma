(ns luma.trie
  (:require [clojure.string :as str]
            [luma.util :refer [lazy-mapcat]])
  #?(:clj (:import (clojure.lang ILookup IPersistentSet))))

(defprotocol ITrie
  (search [trie s] "Find all strings that start with s"))

(declare make-trie)

(defn ^:private empty-trie [] (make-trie "" false {}))

(defn ^:private trie-lookup [{:keys [value contains children]} k not-found]
  (let [[c & cs] k]
    (cond
      (and (not c) contains) value
      (and c (contains? children c)) (get (get children c) cs not-found)
      :else not-found)))

(defn ^:private trie-disjoin [{:keys [value contains children] :as trie} k]
  (let [[c & cs] k]
    (cond
      (not c) {:value value, :contains false, :children children}
      (contains? children c) {:value value, :contains contains, :children (update children c disj cs)}
      :else trie)))

(defn ^:private trie-conj [{:keys [value contains children]} s]
  (let [[c & cs] s]
    (cond
      (not c) {:value value, :contains true, :children children}
      (contains? children c) {:value value, :contains contains, :children (update children c conj cs)}
      :else {:value    value
             :contains contains
             :children (assoc children c (conj (make-trie (str value c) false {}) cs))})))

(defn ^:private trie-seq [{:keys [value contains children]}]
  (let [ks (sort (keys children))
        subseq (lazy-mapcat #(seq (get children %)) ks)]
    (if contains
      (cons value subseq)
      subseq)))

(defn ^:private trie-search [{:keys [children] :as trie} s]
  (let [[c & cs] s]
    (cond
      (not c) (trie-seq trie)
      (contains? children c) (search (get children c) cs)
      :else [])))

#?(:clj
   (deftype Trie [trie]
     ILookup
     (valAt [this k]
       (.valAt this k nil))
     (valAt [this k not-found]
       (trie-lookup trie k not-found))

     IPersistentSet
     (disjoin [this key]
       (Trie. (trie-disjoin trie key)))
     (contains [this key]
       (let [not-found (Object.)]
         (not= not-found (get this key not-found))))
     (get [this key]
       (.valAt this key))
     (count [this]
       (count (seq this)))
     (cons [this o]
       (Trie. (trie-conj trie o)))
     (empty [this]
       (empty-trie))
     (equiv [this o]
       (= (seq this) (seq o)))
     (seq [self]
       (trie-seq trie))

     ITrie
     (search [this s]
       (trie-search trie s)))

   :cljs
   (deftype Trie [trie]
     cljs.core/ILookup
     (-lookup [this k]
       (-lookup this k nil))
     (-lookup [this k not-found]
       (trie-lookup trie k not-found))

     cljs.core/ISet
     (-disjoin [this key]
       (Trie. (trie-disjoin trie key)))

     cljs.core/ICounted
     (-count [this]
       (count (seq this)))

     cljs.core/ICollection
     (-conj [this o]
       (Trie. (trie-conj trie o)))

     cljs.core/IEmptyableCollection
     (-empty [this]
       (empty-trie))

     cljs.core/IEquiv
     (-equiv [this o]
       (= (seq this) (seq o)))

     cljs.core/ISeqable
     (-seq [this]
       (trie-seq trie))

     ITrie
     (search [this s]
       (trie-search trie s))))

(defn ^:private make-trie [value contains children]
  (Trie. {:value    value
          :contains contains
          :children children}))

(defn trie
  ([]
   (empty-trie))
  ([strs]
   (into (empty-trie) (set (map str/lower-case strs)))))
