(ns taoclj.myns1
  (:require [taoclj.via :refer [deftable]]
            [taoclj.myns2 :refer [mypage]]))


(mypage {})


(deftable routes :public ["/" {:get mypage}])


routes

(let [handler (-> routes (nth 0) (second) :get first)]
  (handler {}))



(def handler [{:get (fn [] mypage)}])

(((-> handler first :get)) {})
