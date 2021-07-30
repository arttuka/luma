(ns luma.util
  (:require #?(:clj  [clojure.core.async :refer [>! <! <!! alts! put! go go-loop chan dropping-buffer sliding-buffer timeout]]
               :cljs [clojure.core.async :refer [<! alts! put! chan sliding-buffer timeout] :refer-macros [go]])
            [#?(:clj  clj-time.core
                :cljs cljs-time.core)
             :as time]))

(defn lazy-mapcat [f coll]
  (lazy-seq
   (when (seq coll)
     (concat (f (first coll))
             (lazy-mapcat f (rest coll))))))

#?(:clj
   (defn ->hex [bytes]
     (apply str (for [b bytes] (format "%02x" b)))))

#?(:clj
   (defn throttle [f calls-per-second]
     (let [c (chan (dropping-buffer calls-per-second))
           ms (/ 1000 calls-per-second)]
       (go-loop []
         (>! c ::allowed)
         (<! (timeout ms))
         (recur))
       (fn [& args]
         (<!! c)
         (apply f args)))))

(defn update! [m k f & args]
  (assoc! m k (apply f (get m k) args)))

(defn map-values
  [m f]
  (persistent!
   (reduce-kv (fn [acc k v]
                (assoc! acc k (f v)))
              (transient {})
              m)))

(defn map-by
  ([keyfn coll]
   (map-by keyfn identity coll))
  ([keyfn valfn coll]
   (into {} (map (juxt keyfn valfn)) coll)))

(defn older-than-1-month? [date]
  (time/before? date (time/minus (time/now) (time/months 1))))

(defn group-by-kv [keyfn valfn coll]
  (persistent!
   (reduce (fn [acc x]
             (let [k (keyfn x)
                   v (valfn x)]
               (update! acc k (fnil conj []) v)))
           (transient {})
           coll)))

(defn debounce [f ms]
  (let [c (chan (sliding-buffer 1))]
    (go
      (loop [args (<! c)]
        (let [[val port] (alts! [c (timeout ms)])]
          (when (not= port c)
            (apply f args))
          (recur (if (= port c)
                   val
                   (<! c))))))
    (fn [& args]
      (put! c (or args [])))))

#?(:cljs (defn wrap-on-change [f]
           (fn [^js/Event event]
             (f (.. event -target -value)))))

(defn on-mobile [theme]
  ((get-in theme [:breakpoints :down]) "sm"))

(defn on-desktop [theme]
  ((get-in theme [:breakpoints :up]) "sm"))

#?(:clj (defmacro when-let+ [bindings & body]
          (assert (vector? bindings) "when-let+ requires a vector for its bindings")
          (assert (even? (count bindings)) "when-let+ requires an even number of forms in binding vector")
          (if-let [[sym expr & more] (seq bindings)]
            `(when-let [~sym ~expr]
               (when-let+ ~(vec more)
                 ~@body))
            `(do ~@body))))

(defn make-classes [prefix classes]
  (into {} (for [c classes]
             [c (str prefix (name c))])))

(defn set-classes [classes styles]
  (into {} (for [[k v] styles
                 :let [k' (cond
                            (string? k) k
                            (= :root k) (str "&." (:root classes))
                            :else (str "& ." (get classes k)))]]
             [k' v])))