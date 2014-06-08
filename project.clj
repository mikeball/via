(defproject org.taoclj/via "0.0.1-SNAPSHOT"
  :description "A ring routing library with integrated authorization"

  :url "http://github.com/mikeball/via"
  
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clout "1.2.0"]
                 [ring/ring-core "1.3.0"]]

  ; unknown what we're using ring-devel for
  ;:profiles {:dev {:dependencies [[ring/ring-devel "1.3.0"]]}}

)