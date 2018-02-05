(ns luma.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [luma.core-test]))

(doo-tests 'luma.core-test)
