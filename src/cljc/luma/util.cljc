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
