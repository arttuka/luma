(ns luma.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [luma.trie-test]
            [luma.util-test]))

(doo-tests 'luma.trie-test 'luma.util-test)
