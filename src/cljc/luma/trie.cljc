(ns luma.trie
  (:require [clojure.string :as str]
            [luma.util :refer [lazy-mapcat]]
            #?(:cljs [cljs.core :refer [IWithMeta IMeta IFn ISeqable IEquiv IEmptyableCollection
                                        ICollection ICounted ISet ILookup]]))
  #?(:clj
     (:import (clojure.lang IObj IMeta IFn ILookup IPersistentSet))))

(defprotocol ITrie
  (search [trie s] "Find all strings that start with s"))

(declare empty-trie)

(defn ^:private trie-lookup [{::keys [end-of-word] :as trie} [c & cs]]
  (if c
    (when (contains? trie c)
      (recur (get trie c) cs))
    end-of-word))

(defn ^:private clean-trie [trie c]
  (if (empty? (get trie c))
    (dissoc trie c)
    trie))

(defn ^:private trie-disj [trie [c & cs]]
  (cond
    (nil? c) (dissoc trie ::end-of-word)
    (contains? trie c) (-> trie
                           (update c trie-disj cs)
                           (clean-trie c))
    :else trie))

(defn ^:private trie-conj [trie [c & cs]]
  (if c
    (update trie c trie-conj cs)
    (assoc trie ::end-of-word true)))

(defn ^:private trie-seq [{::keys [end-of-word] :as trie} prefix]
  (let [ks (sort (remove #{::end-of-word} (keys trie)))
        subseq (lazy-mapcat #(trie-seq (get trie %) (str prefix %)) ks)]
    (if end-of-word
      (cons prefix subseq)
      (seq subseq))))

(defn ^:private trie-search [trie prefix [c & cs]]
  (cond
    (nil? c) (trie-seq trie prefix)
    (contains? trie c) (recur (get trie c) (str prefix c) cs)
    :else ()))

#?(:clj
   (deftype Trie [_meta trie]
     ILookup
     (valAt [this k]
       (.valAt this k nil))
     (valAt [_ k not-found]
       (if (trie-lookup trie k) k not-found))

     IPersistentSet
     (disjoin [_ key]
       (Trie. _meta (trie-disj trie key)))
     (contains [_ key]
       (boolean (trie-lookup trie key)))
     (get [this key]
       (.valAt this key nil))
     (count [this]
       (count (seq this)))
     (cons [_ o]
       (Trie. _meta (trie-conj trie o)))
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
       (trie-search trie "" s))

     IMeta
     (meta [_]
       _meta)

     IObj
     (withMeta [_ new-meta]
       (Trie. new-meta trie)))

   :cljs
   (deftype Trie [_meta trie]
     ILookup
     (-lookup [this k]
       (-lookup this k nil))
     (-lookup [_ k not-found]
       (if (trie-lookup trie k) k not-found))

     ISet
     (-disjoin [_ key]
       (Trie. _meta (trie-disj trie key)))

     ICounted
     (-count [this]
       (count (seq this)))

     ICollection
     (-conj [_ o]
       (Trie. _meta (trie-conj trie o)))

     IEmptyableCollection
     (-empty [_]
       empty-trie)

     IEquiv
     (-equiv [this o]
       (= (seq this) (seq o)))

     ISeqable
     (-seq [_]
       (trie-seq trie ""))

     ITrie
     (search [_ s]
       (trie-search trie "" s))

     IFn
     (-invoke [_ s]
       (trie-search trie "" s))

     IMeta
     (-meta [_]
       _meta)

     IWithMeta
     (-with-meta [_ new-meta]
       (Trie. new-meta trie))))

(def ^:private empty-trie (Trie. nil {}))

(defn trie
  ([]
   empty-trie)
  ([strs]
   (into empty-trie (set (map str/lower-case strs)))))
