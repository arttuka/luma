(ns luma.util
  (:require [clojure.core.async :refer [<! go]]))

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
  (if (instance? #?(:clj Throwable
                    :cljs js/Error)
                 e)
    (throw e)
    e))

(defmacro <? [port]
  `(throw-if-error (<! ~port)))

#?(:clj
   (defn ->hex [#^bytes bytes]
     (.toString (BigInteger. 1 bytes) 16)))
