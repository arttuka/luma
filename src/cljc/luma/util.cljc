(ns luma.util
  (:require #?(:clj  [clojure.core.async :refer [>! <! <!! alts! put! go go-loop chan dropping-buffer sliding-buffer timeout]]
               :cljs [clojure.core.async :refer [<! alts! put! chan sliding-buffer timeout] :refer-macros [go]])
            #?(:cljs [oops.core :refer [oget]])
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
               (assoc! acc k (conj (get acc k []) v))))
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
           (fn [event]
             (f (oget event "target" "value")))))

(defn on-mobile [theme]
  ((get-in theme [:breakpoints :down]) "sm"))

(defn on-desktop [theme]
  ((get-in theme [:breakpoints :up]) "sm"))
