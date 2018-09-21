(ns luma.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [luma.events-test]
            [luma.subs-test]
            [luma.trie-test]
            [luma.util-test]))

(doo-tests 'luma.events-test 'luma.subs-test 'luma.trie-test 'luma.util-test)
