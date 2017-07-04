(ns parenity.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [parenity.core-test]))

(doo-tests 'parenity.core-test)
