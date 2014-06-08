(ns taoclj.via.request
  (:require [clojure.string :refer [lower-case]]))


(defn sse-request?
  "Checks for Server Sent Events request, ie the accept header
  for text/event-stream value. Warning, W3C spec doesn't seem
  to require this, only suggests it. Chrome and FireFox seem
  to send it so we can thus dispatch in the majority of cases.

  (sse-request? {:headers {\"accept\" \"text/event-stream\"}})
  => true
  "
  [request]

  (let [accept (get-in request [:headers "accept"])]
    (if-not accept false
      (= (lower-case accept) "text/event-stream"))))


(defn websocket-request?
  "Websocket requests have a connection header set to
  upgrade and an upgrade header set to websocket"
  [request]
  (let [connection (get-in request [:headers "connection"])
        upgrade (get-in request [:headers "upgrade"])]
    (if-not (and connection upgrade) false
      (and (= (lower-case connection) "upgrade")
           (= (lower-case upgrade) "websocket")))))


(defn handler-method
  "Given a ring request, returns the appropiate handler method
   :get, :post, :put, :patch, :delete, :websocket, :sse"
  [request]

  (cond (websocket-request? request) :websocket
        (sse-request? request) :sse
        :default (request :request-method)))


