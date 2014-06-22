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
  (are [method]
       (= :ok ((first (routing/build-handler-roles-list
                       method
                       (fn [r] :ok)
                       [:a :b])) {}))
       :get
       :post
       :put
       :delete
       :head
       :options
       :websocket
       :sse ))

(deftest special-and-unknown-keys-are-passed
  (are [key value]
       (= value
          (first (routing/build-handler-roles-list key value [])))
       :name 'n
       :regex 'r
       :unknown 'u))


(deftest roles-are-set-on-relevant-methods
  (is (= '(:role1 :role2)
         (-> (routing/set-route-roles ["/" {:get 'h :name "x"}] [:role1 :role2])
             second
             :get
             rest) )))


(def built-routes
  (routing/build-routes '(:a :b ["/a" {:get h1}] ["/b" {:get h2}]
                          :c :d ["/c" {:get h3}] ["/d" {:get h4}])))

(deftest build-sets-roles
  (are [place-fn expected-roles]
       (= expected-roles
          (-> built-routes place-fn second :get rest))

       first '(:a :b)
       second '(:a :b)
       (nth 2) '(:c :d)
       (nth 3) '(:c :d)))













;; (deftest build-route-validates-route-structure
;;   ;; route must have count of 2
;;   ;; string must be in first postion
;;   ;; map must be in second postion
;;   (is (thrown? Exception (/ 1 0))))



