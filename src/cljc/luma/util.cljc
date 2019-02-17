(ns luma.util
  (:require [clojure.core.async :as async :refer [>! <! go go-loop chan dropping-buffer timeout]]
            #?(:clj [garden.stylesheet :refer [at-media]])
            #?(:clj [garden.units :refer [px percent]])
            #?(:cljs [oops.core :refer [oget]])
            [#?(:clj  clj-time.core
                :cljs cljs-time.core)
             :as time]))

(defn ^:private if-cljs [env then else]
  (if (:ns env)
    then
    else))

(defn lazy-mapcat [f coll]
  (lazy-seq
   (when (seq coll)
     (concat (f (first coll))
             (lazy-mapcat f (rest coll))))))

(defmacro go-ex
  "Like go, but catches and returns any exception"
  [& body]
  `(go (try
         ~@body
         (catch ~(if-cljs &env 'js/Error 'java.lang.Throwable) t#
           t#))))

(defn throw-if-error [e]
  (if (instance? #?(:clj  Throwable
                    :cljs js/Error)
                 e)
    (throw e)
    e))

(defmacro <? [port]
  `(throw-if-error (<! ~port)))

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
         (async/<!! c)
         (apply f args)))))

(defn map-values
  [m f]
  (persistent!
   (reduce-kv (fn [acc k v]
                (assoc! acc k (f v)))
              (transient {})
              m)))

(defn map-by
  [f coll]
  (into {} (map (juxt f identity)) coll))

(defn older-than-1-month? [date]
  (time/before? date (time/minus (time/now) (time/months 1))))

(def mobile-max-width 400)

#?(:clj (defn when-mobile [& styles]
          (apply at-media {:max-width (px mobile-max-width)} styles)))

#?(:clj (defn when-desktop [& styles]
          (apply at-media {:min-width (px (inc mobile-max-width))} styles)))

#?(:cljs (defn mobile? []
           (<= (oget js/window "innerWidth") mobile-max-width)))
