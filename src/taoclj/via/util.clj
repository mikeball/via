(ns taoclj.via.util)


(defn in?
  "Does an element exists in a sequence? Returns nil if nothing found."
  [sequence element] ;; potentially change the order of these
  (some #(= element %) sequence))

(defn any-matches?
  "Are there any matching elements between sequences?"
  [sequence1 sequence2]
  (some #(in? sequence1 %) sequence2))
