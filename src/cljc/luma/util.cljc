(ns luma.util
  (:require [clojure.core.async :as async :refer [>! <! go go-loop chan dropping-buffer timeout]]))

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
