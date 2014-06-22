(ns taoclj.via.routing
  (:require [clojure.string :as string]
            [taoclj.via.routing.path :as path]
            [taoclj.via.util :as util]))


(defn match-route
  "Determines if a single route matches a given request.
   Returns {:handler resulting-handler
            :path-params {:pram-name \"val\"}} "
  [not-authorized route request-path request-method request-roles]

  ;; check for path matches
  (let [path-match (path/match (first route) request-path)]
    (if-not path-match nil

      ;; check for method matches
      (let [method-match (request-method (second route))]
        (if-not method-match

                ;return method not allowed handler
                {:handler (fn [_] {:status 405})}

                ;; check for allowed role
                (let [route-roles (rest method-match)]
                  (if-not (or (util/in? route-roles :public)
                              (util/any-matches? route-roles request-roles))
                    {:handler not-authorized}

                    ;; return the matched handler & params parsed from path
                    {:handler (first method-match)
                     :path-params path-match})))))))



(defn match
  "Returns first matching route in the routing table, given a path, method and roles.
   You must first initialize the routing table, not found and not authorized handlers!!

  ;; No match found returns
  ;; => {:handler your-configured-not-found-handler}

  ;; No match between the allowed method roles and request roles returns
  ;; => {:handler your-configured-not-authorized-handler}

  ;; A successful match returns the relevant handler, as well as params parsed from the path
  ;; => {:handler your-matched-handler
  ;;     :path-params {:a \"1\"} }
"
  [routes not-found not-authorized request-path request-method request-roles]
  (let [match (first (filter #(not (nil? %))
                             (map #(match-route not-authorized % request-path request-method request-roles)
                                  routes)))]

    (if-not (nil? match) match
            {:handler not-found})))






;; TODO validate supplied route structure
;; (defn route-errors [route]
;;   (and (if-not 2 (count role)))
;; )

;; (let [valid (and (= 2 (count route)))]
;;     (if-not valid (throw (Exception. "*** Each route must have count of 2! ***")))
;;     )



(defn build-handler-roles-list [key value roles]
  (if-not (util/in? [:get :post :put :delete :head :options :websocket :sse] key)
    (list value)
    (cons (fn [r] (value r)) roles)))


(defn set-route-roles
  "Given a route, set the roles on all relevant methods"
  [route roles]
  (assoc route 1 (into {} (for [[key val] (second route)]
                            [key (build-handler-roles-list key val roles)]))))


(defn build-routes [data]
  (loop [routes '()
         route-data data]
    (let [roles (take-while keyword? route-data)
          simple-routes (take-while vector? (nthnext route-data (count roles)))
          skip (+ (count roles) (count simple-routes))
          verbose-routes (for [route simple-routes] (set-route-roles route roles))]
      (if (= 0 skip) routes
          (recur (concat routes verbose-routes) (nthnext route-data skip))))))



(defn build [& route-data]
  (build-routes route-data))
