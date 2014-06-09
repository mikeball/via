(ns taoclj.via-test
  (:require [clojure.test :refer :all]
            [taoclj.via :as via]))


(deftest require-routes-setting
  (is (thrown? IllegalArgumentException (via/fn-dispatch {}))))


(deftest return-generic-not-found-if-handler-unspecified
  (is (= {:status 404}
         ((via/fn-dispatch {:routes '()})

          {:uri "/" :request-method :get}))))


(deftest return-specified-not-found-if-handler-supplied
  (is (= {:status 404 :body "nf"}
         ((via/fn-dispatch {:routes     '()
                            :not-found  (fn [_] {:status 404 :body "nf"})})
          {:uri "/" :request-method :get}))))


(deftest return-generic-not-authorized-if-handler-unspecified
  (is (= {:status 403}
         ((via/fn-dispatch {:routes '(["/" {:get ('h :a-role)}])})
          {:uri "/" :request-method :get}))))


(deftest return-specified-not-authorized-if-handler-supplied
  (is (= {:status 403 :body "na"}
         ((via/fn-dispatch {:routes         '(["/" {:get ('h :a-role)}])
                            :not-authorized (fn [_] {:status 403 :body "na"})})
          {:uri "/" :request-method :get}))))


(deftest generated-dispatch-returns-not-found-on-no-path-match
   (is (= {:status 404}
          ((via/fn-dispatch {:routes         '(["/a" {}])
                             :not-found      (fn [r] {:status 404})})

           {:uri "/b" :request-method :get}))))


(deftest generated-dispatch-returns-method-not-allowed-on-no-method-match
   (is (= {:status 405}
          ((via/fn-dispatch {:routes         '(["/a" {}])})
           {:uri "/a" :request-method :get}))))


(deftest generated-dispatch-enforces-authorization
   (is (= {:status 403 :body "na"}
          ((via/fn-dispatch {:routes         '(["/a" {:get ('h :a-role)}])
                             :not-authorized (fn [_] {:status 403 :body "na"})})
           {:uri "/a"
            :request-method :get
            :user nil}))))


(deftest generated-dispatch-allows-public-path-methods
   (is (= {:status 200 :body "ok"}
          ((via/fn-dispatch {:routes       [["/a" {:get [(fn [r] {:status 200 :body "ok"}) :public]}]]

                             ; :authenticate (fn [_] {:roles [:a-role]})

                             })
           {:uri "/a"
            :request-method :get}))))


(deftest generated-dispatch-allows-valid-roles
   (is (= {:status 200 :body "ok"}
          ((via/fn-dispatch {:routes       [["/a" {:get [(fn [r] {:status 200 :body "ok"}) :a-role]}]]
                             :authenticate (fn [_] {:roles [:a-role]})})
           {:uri "/a"
            :request-method :get}))))


(deftest path-parameters-are-passed-to-handlers
   (is (= "1"
          (:body ((via/fn-dispatch {:routes [["/a/:id"
                                              {:get [(fn [r] {:status 200 :body (-> r :params :id)})
                                                     :public]}]]})
                  {:uri "/a/1"
                   :request-method :get})))))



; (clojure.test/run-tests 'taoclj.via-test)
















