(ns luma.util)

(defmacro when-let+
  "Like when-let, but allows multiple bindings and returns nil if any of the bindings is nil"
  [bindings & body]
  (if (seq bindings)
    `(when-let [~@(take 2 bindings)]
       (when-let+ ~(vec (drop 2 bindings)) ~@body))
    `(do ~@body)))

(defn grouping [group-keys sub-key coll]
  (let [gp (group-by #(select-keys % group-keys) coll)]
    (for [[k v] gp]
      (assoc k sub-key (map #(apply dissoc % group-keys) v)))))

(defn lazy-mapcat [f coll]
  (lazy-seq
    (when (seq coll)
      (concat (f (first coll))
              (lazy-mapcat f (rest coll))))))

(defn map-values [m f]
  (when m (into {} (map (juxt key (comp f val))) m)))

(defn map-by
  ([keyfn coll]
    (map-by keyfn identity coll))
  ([keyfn valfn coll]
   (into {} (map (juxt keyfn valfn)) coll)))
