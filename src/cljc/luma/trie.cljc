(ns luma.trie
  (:require [clojure.string :as str]
            [luma.util :refer [lazy-mapcat]])
  #?(:clj
     (:import (clojure.lang IFn ILookup IPersistentSet))))

(defprotocol ITrie
  (search [trie s] "Find all strings that start with s"))

(declare empty-trie)

(defn ^:private trie-lookup [{:keys [end-of-word children]} [c & cs]]
  (if c
    (when (contains? children c)
      (recur (get children c) cs))
    end-of-word))

(defn ^:private clean-trie [{:keys [children] :as trie} c]
  (if (empty? (get children c))
    (let [trie (update trie :children dissoc c)]
      (if (empty? (:children trie))
        (dissoc trie :children)
        trie))
    trie))

(defn ^:private trie-disj [{:keys [children] :as trie} [c & cs]]
  (cond
    (not c) (dissoc trie :end-of-word)
    (contains? children c) (-> trie
                             (update-in [:children c] trie-disj cs)
                             (clean-trie c))
    :else trie))

(defn ^:private trie-conj [trie [c & cs]]
  (if (not c)
    (assoc trie :end-of-word true)
    (update-in trie [:children c] trie-conj cs)))

(defn ^:private trie-seq [{:keys [end-of-word children]} prefix]
  (let [ks (sort (keys children))
        subseq (lazy-mapcat #(trie-seq (get children %) (str prefix %)) ks)]
    (if end-of-word
      (cons prefix subseq)
      subseq)))

(defn ^:private trie-search [{:keys [children] :as trie} prefix [c & cs]]
  (cond
    (not c) (trie-seq trie prefix)
    (contains? children c) (recur (get children c) (str prefix c) cs)
    :else []))

#?(:clj
   (deftype Trie [trie]
     ILookup
     (valAt [this k]
       (.valAt this k nil))
     (valAt [_ k not-found]
       (if (trie-lookup trie k) k not-found))

     IPersistentSet
     (disjoin [_ key]
       (Trie. (trie-disj trie key)))
     (contains [_ key]
       (boolean (trie-lookup trie key)))
     (get [this key]
       (.valAt this key nil))
     (count [this]
       (count (seq this)))
     (cons [_ o]
       (Trie. (trie-conj trie o)))
     (empty [_]
       empty-trie)
     (equiv [this o]
       (= (seq this) (seq o)))
     (seq [_]
       (trie-seq trie ""))

     ITrie
     (search [_ s]
       (trie-search trie "" s))

     IFn
     (invoke [_ s]
       (trie-search trie "" s)))

   :cljs
   (deftype Trie [trie]
     cljs.core/ILookup
     (-lookup [this k]
       (-lookup this k nil))
     (-lookup [_ k not-found]
       (if (trie-lookup trie k) k not-found))

     cljs.core/ISet
     (-disjoin [_ key]
       (Trie. (trie-disj trie key)))

     cljs.core/ICounted
     (-count [this]
       (count (seq this)))

     cljs.core/ICollection
     (-conj [_ o]
       (Trie. (trie-conj trie o)))

     cljs.core/IEmptyableCollection
     (-empty [_]
       empty-trie)

     cljs.core/IEquiv
     (-equiv [this o]
       (= (seq this) (seq o)))

     cljs.core/ISeqable
     (-seq [_]
       (trie-seq trie ""))

     ITrie
     (search [_ s]
       (trie-search trie "" s))

     cljs.core/IFn
     (-invoke [_ s]
       (trie-search trie "" s))))

(def ^:private empty-trie (Trie. {}))

(defn trie
  ([]
   empty-trie)
  ([strs]
   (into empty-trie (set (map str/lower-case strs)))))
