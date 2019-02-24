(ns luma.test-util
  (:require [clojure.core.async :as async]
            #?(:cljs [cljs.test :refer-macros [async]])))

(defn test-async
  [ch]
  #?(:clj  (async/<!! ch)
     :cljs (async done
             (async/take! ch (fn [_] (done))))))
