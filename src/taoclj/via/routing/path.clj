(ns taoclj.via.routing.path
  (:require [clout.core :as clout]))


(defn match
  "Returns nil if no match. Matched paths returns map containing matched params"
  [pattern given-path]
  (clout/route-matches (clout/route-compile pattern)
                       {:uri given-path}))