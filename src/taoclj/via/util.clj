(ns taoclj.via.util
  (:require [clojure.string :as string]))


(defn in?
  "Does an element exists in a sequence? Returns nil if nothing found."
  [sequence element] ;; potentially change the order of these
  (some #(= element %) sequence))

(defn any-matches?
  "Are there any matching elements between sequences?"
  [sequence1 sequence2]
  (some #(in? sequence1 %) sequence2))


(defn varify
  "Extracts var from function reference to allow reference by name."
  [function]
  (let [t (str (type function))]
    (cond (not (fn? function)) function ; ignore anything that's not a function

          (and (fn? function)
               (.contains t "$fn__")) function ; ignore anonomous functions

          :else
          ; refer to the fuction by name so that function re defn's are picked up during development
          (-> t
              (string/replace "class " "")
              (string/replace "_" "-")
              (string/replace "$" "/")
              (symbol)
              (resolve)))))
