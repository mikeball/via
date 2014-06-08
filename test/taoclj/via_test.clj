(ns taoclj.via-test
  (:require [clojure.test :refer :all]
            [taoclj.via :as via]))


(deftest require-routes-setting
  (is (thrown? IllegalArgumentException (via/fn-dispatch {:content-type "ct"}))))


(deftest require-content-type-setting
  (is (thrown? IllegalArgumentException (via/fn-dispatch {:routes '(["/" {}])}))))


(deftest return-generic-not-found-if-handler-unspecified
  (is (= {:status 404 :headers {"Content-Type" "ct"}}
         ((via/fn-dispatch {:routes         '()
                            :content-type   "ct"})
          {:uri "/" :request-method :get}))))


(deftest return-specified-not-found-if-handler-supplied
  (is (= {:status 404 :body "nf" :headers {"Content-Type" "ct"}}
         ((via/fn-dispatch {:routes         '()
                            :content-type   "ct"
                            :not-found      (fn [_] {:status 404 :body "nf"})})
          {:uri "/" :request-method :get}))))


(deftest return-generic-not-authorized-if-handler-unspecified
  (is (= {:status 403 :headers {"Content-Type" "ct"}}
         ((via/fn-dispatch {:routes         '(["/" {:get ('h :a-role)}])
                            :content-type   "ct"})
          {:uri "/" :request-method :get}))))


(deftest return-specified-not-authorized-if-handler-supplied
  (is (= {:status 403 :body "na" :headers {"Content-Type" "ct"}}
         ((via/fn-dispatch {:routes         '(["/" {:get ('h :a-role)}])
                            :content-type   "ct"
                            :not-authorized (fn [_] {:status 403 :body "na"})})
          {:uri "/" :request-method :get}))))


(deftest content-type-is-set-on-response
  (is (= {:status 200 :body "hi" :headers {"Content-Type" "ct"}}
         ((via/fn-dispatch {:routes         [["/" {:get [(fn [_] {:status 200 :body "hi"}) :public]}]]
                            :content-type   "ct"})
          {:uri "/" :request-method :get}))))


(deftest generated-dispatch-returns-not-found-on-no-path-match
   (is (= {:status 404 :headers {"Content-Type" "ct"}}
          ((via/fn-dispatch {:routes         '(["/a" {}])
                             :content-type   "ct"
                             :not-found      (fn [r] {:status 404})})

           {:uri "/b" :request-method :get}))))


(deftest generated-dispatch-returns-method-not-allowed-on-no-method-match
   (is (= {:status 405 :headers {"Content-Type" "ct"}}
          ((via/fn-dispatch {:routes         '(["/a" {}])
                             :content-type   "ct"})
           {:uri "/a" :request-method :get}))))


(deftest generated-dispatch-enforces-authorization
   (is (= {:status 403 :headers {"Content-Type" "ct"} :body "na"}
          ((via/fn-dispatch {:routes         '(["/a" {:get ('h :a-role)}])
                             :content-type   "ct"
                             :not-authorized (fn [_] {:status 403 :body "na"})})
           {:uri "/a"
            :request-method :get
            :user nil}))))


 (deftest generated-dispatch-allows-valid-roles
   (is (= {:status 200
           :headers {"Content-Type" "ct"}
           :body "ok"}
          ((via/fn-dispatch {:routes       [["/a" {:get [(fn [r] {:status 200 :body "ok"}) :a-role]}]]
                             :content-type "ct"
                             :authenticate (fn [_] {:roles [:a-role]})})
           {:uri "/a"
            :request-method :get}))))


(deftest path-parameters-are-passed-to-handlers
   (is (= "1"
          (:body ((via/fn-dispatch {:routes [["/a/:id"
                                              {:get [(fn [r] {:status 200 :body (-> r :params :id)})
                                                     :public]}]]

                                    :content-type "ct"})
                  {:uri "/a/1"
                   :request-method :get})))))



; (clojure.test/run-tests 'taoclj.via-test)
















