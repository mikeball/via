(ns taoclj.via.routing-test
  (:require [clojure.test :refer :all]
            [taoclj.via.routing :as routing]))


(deftest unmatched-route-path-returns-nil
  (are [route path method]
       (nil? (routing/match-route 'nah route path method []))
       ["/" {}] "/nope" :get )) ;; no path match


(deftest unauthorized-route-returns-not-authorized-handler
  (are [route path method roles]
       (= 'nah (:handler (routing/match-route 'nah route path method roles)))

       ["/" {:get ['h :myrole]}] "/" :get []
       ["/" {:get ['h :myrole]}] "/" :get [:notmyrole]))


(deftest matched-returns-handler
  (are [route path method roles] (= 'h
                                    (:handler (routing/match-route 'nah route path method roles)))
       ["/" {:get ['h :myrole]}] "/" :get [:myrole]))


(deftest routes-marked-public-ignore-role-checks
  (are [route path method roles] (= 'h (:handler (routing/match-route 'nah route path method roles)))
       ["/" {:get ['h :public]}] "/" :get []))


(deftest path-params-are-returned
  (are [route-path request-path expected-params]
       (= expected-params (:path-params (routing/match-route 'nah
                                                             [route-path {:get ['h :myrole]}]
                                                             request-path :get [:myrole])))
       "/a" "/a" {}
       "/:a" "/1" {:a "1"}
       "/:a/b/:c" "/a/b/c"  {:a "a" :c "c"}))



;; Matching sequence of routes

(def routes [["/a" {:get ['h1 :public]}]
             ["/b" {:get ['h2 :x :y]}]])


(deftest unmatched-path-returns-not-found-handler
  (are [request-path request-method request-roles]
       (= 'nf (:handler (routing/match routes 'nf 'na request-path request-method request-roles)))
       "/" :get []))



(deftest unmatched-method-returns-method-not-allowed-handler
  (is (= {:status 405}
         (let [match (routing/match routes 'nf 'na "/a" :post [])
               handler (:handler match)]
           (handler {})))))




(deftest matched-returns-handler
  (are [request-path request-method request-roles expected]
       (= expected (:handler (routing/match routes 'nf 'na request-path request-method request-roles)))
       "/a" :get [] 'h1 ;; path matched, public route
       "/b" :get [:y] 'h2))





;; Routing Builder testings

(deftest handlers-are-prepended-to-list-of-roles
  (are [method handler roles expected]
       (= expected (routing/build-handler-roles-list method handler roles))

       :get 'h [:a :b] '(h :a :b)
       :post 'h [:a :b] '(h :a :b)
       :name "n" [:a :b] '("n") ;; name key does not get roles appended
       :regex "r" [:a :b] '("r") ;; regex key does not append roles

       :unknown "u" [:a :b] '("u") ; unknown keys are left alone

       ))


(deftest roles-are-set-on-relevant-methods
  (is (= (routing/set-route-roles ["/" {:get 'h :name "x"}] [:role1 :role2])
         ["/" {:get '(h :role1 :role2) :name '("x")}])))



(deftest build-sets-roles
  (is (= (routing/build-routes '(:a :b ["/a" {:get h1}] ["/b" {:get h2}]))
         '(["/a" {:get (h1 :a :b)}] ["/b" {:get (h2 :a :b)}])))

  (is (= (routing/build-routes '(:a :b ["/a" {:get h1}] ["/b" {:get h2}]
                                 :c :d ["/c" {:get h3}] ["/d" {:get h4}]))
         '(["/a" {:get (h1 :a :b)}] ["/b" {:get (h2 :a :b)}]
           ["/c" {:get (h3 :c :d)}] ["/d" {:get (h4 :c :d)}]))))





; anonomous functions are not var quoted
(deftest anonomous-functions-are-not-var-quoted
  (is (=
         (routing/set-route-roles ["/" {:get 'h :name "x"}] [:role1 :role2])

         ["/" {:get '(h :role1 :role2) :name '("x")}]

         )))




; vars are not var quoted


; direct named symbols are var quoted








;; (deftest build-route-validates-route-structure
;;   ;; route must have count of 2
;;   ;; string must be in first postion
;;   ;; map must be in second postion
;;   (is (thrown? Exception (/ 1 0))))



