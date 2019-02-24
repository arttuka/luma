;; This test runner is intended to be run from the command line
(ns luma.figwheel-test-runner
  (:require
   [figwheel.main.testing :refer-macros [run-tests]]

   ;; CLJC
   [luma.trie-test]
   [luma.util-test]

   ;; CLJS
   [luma.events-test]
   [luma.subs-test]))

(defn -main [& args]
  (run-tests))
