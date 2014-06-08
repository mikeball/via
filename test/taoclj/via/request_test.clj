(ns taoclj.via.request-test
  (:require [clojure.test :refer :all]
            [taoclj.via.request :as request]))


(deftest server-sent-events-requests-are-detected
  (are [given expected]
       (= (request/sse-request? given) expected)
       {} false
       {:request-method :get} false
       {:headers {"accept" "text/event-stream"}} true
       {:headers {"accept" "text/Event-Stream"}} true ))


(deftest websocket-requests-are-detected
  (are [given expected]
       (= (request/websocket-request? given) expected)
       {} false
       {:request-method :get} false
       {:headers {"connection" "upgrade"
                  "upgrade" "websocket"}} true
       ; header values should not be case sensitive
       {:headers {"connection" "Upgrade"
                  "upgrade" "WebSocket"}} true ))


(deftest all-handler-methods-are-supported
  (are [given expected]
       (= (request/handler-method given) expected)

       {:request-method :get} :get
       {:request-method :head} :head
       {:request-method :options} :options
       {:request-method :put} :put
       {:request-method :post} :post
       {:request-method :delete} :delete

       {:headers {"connection" "upgrade"
                  "upgrade" "websocket"}} :websocket

       {:request-method :get
        :headers {"connection" "upgrade"
                  "upgrade" "websocket"}} :websocket

       {:request-method :get
        :headers {"accept" "text/event-stream"}} :sse ))

