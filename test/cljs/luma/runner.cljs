(ns luma.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [luma.trie-test]))

(doo-tests 'luma.trie-test)
