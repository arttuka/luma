(ns luma.util-test
  (:require #?(:clj [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest testing is]])
                    [clojure.core.async :as async :refer [go <! >!]]
                    [luma.test-util :refer [test-async]]
    #?(:clj
                    [luma.util :refer [lazy-mapcat go-ex <?]]
       :cljs [luma.util :refer [lazy-mapcat] :refer-macros [go-ex <?]])))

(deftest lazy-mapcat-test
  (testing "lazy-mapcat"
    (testing "works like mapcat"
      (is (= (mapcat range (range 10)) (lazy-mapcat range (range 10)))))
    (testing "is lazy"
      (let [hits (atom 0)
            s (lazy-mapcat (fn [_] (swap! hits inc) [1]) (range 10))]
        (is (zero? @hits))
        (is (= [1 1 1 1 1 1] (take 6 s)))
        (is (= 6 @hits))))))

(def ex #?(:clj  (Exception. "BOOM")
           :cljs (js/Error. "BOOM")))

(deftest go-ex-test
  (testing "go-ex"
    (testing "works like go"
      (let [ch (async/chan 1)
            result-ch (go-ex (<! ch))]
        (test-async
          (go
            (>! ch ::test)
            (is (= ::test (<! result-ch)))))))
    (testing "catches and returns throwables"
      (let [result-ch (go-ex (throw ex))]
        (test-async
          (go (is (= ex (<! result-ch)))))))))

(deftest <?-test
  (testing "<?"
    (testing "throws throwables"
      (let [ch (async/chan 1)
            result-ch (go (try
                            (<? ch)
                            ::no-exception
                            (catch #?(:clj Exception :cljs js/Error) _
                              ::exception)))]
        (test-async
          (go
            (>! ch ex)
            (is (= ::exception (<! result-ch)))))))
    (testing "lets other values through"
      (let [ch (async/chan 10)
            vals [1 false ::test "test"]]
        (test-async
          (go
            (doseq [v vals] (>! ch v))
            (async/close! ch)
            (doseq [v vals] (is (= v (<! ch))))))))))
