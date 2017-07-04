(ns ^:figwheel-no-load parenity.dev
  (:require
    [parenity.core :as core]
    [devtools.core :as devtools]))

(devtools/install!)

(enable-console-print!)

(core/init!)
