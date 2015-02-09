(ns taoclj.via
  (:require [taoclj.via.util :as util]
            [taoclj.via.routing :as routing]
            [taoclj.via.request :refer [handler-method]]))


(defn default-handler [handler status]
  (if handler
    (util/varify handler)
    (fn [_] {:status status})))


(defn validate [settings]
  (cond (not (sequential? (:routes settings)))
        (throw (IllegalArgumentException.
                ":routes setting is required and must be a sequence of routes"))))




(defn fn-dispatch
  "Build a handler function and enforces role restrictions

  (fn-dispatch {:routes my-routes
                :content-type \"text/html;charset=utf-8\"})
  "
  [settings]

  (validate settings)

  (let [routes           (:routes settings)
        not-found        (default-handler (:not-found settings) 404)
        not-authorized   (default-handler (:not-authorized settings) 403)
        authenticate     (:authenticate settings)
        roles-key        (or (:roles-key settings) :roles)]

    (fn [request]

      (let [user (if authenticate (authenticate request))
            match (routing/match routes
                                 not-found
                                 not-authorized
                                 (:uri request)
                                 (handler-method request)
                                 (roles-key user))
            handler (match :handler)]

        (-> request
            (assoc :user user)
            (assoc :params (merge (request :params)
                                  (match :path-params)))
            (handler))))))



(defmacro deftable [name & route-data]
  `(def ~name (routing/build ~@route-data)))




