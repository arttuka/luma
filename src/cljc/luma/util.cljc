(ns luma.util
  (:require #?(:clj  [clojure.core.async :refer [>! <! <!! alts! put! go go-loop chan dropping-buffer sliding-buffer timeout]]
               :cljs [clojure.core.async :refer [<! alts! put! chan sliding-buffer timeout] :refer-macros [go]])
            #?(:clj [garden.stylesheet :refer [at-media]])
            #?(:clj [garden.units :refer [px percent]])
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
   (defn ->hex [#^bytes bytes]
     (.toString (BigInteger. 1 bytes) 16)))

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

(def mobile-max-width 400)

#?(:clj (defn when-mobile [& styles]
          (apply at-media {:max-width (px mobile-max-width)} styles)))

#?(:clj (defn when-desktop [& styles]
          (apply at-media {:min-width (px (inc mobile-max-width))} styles)))

#?(:cljs (defn mobile? []
           (<= (oget js/window "innerWidth") mobile-max-width)))
