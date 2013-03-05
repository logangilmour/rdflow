(defproject rdflow "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [clj-http "0.6.3"]
                 [org.apache.jena/apache-jena "2.7.3" :extension "pom"]]
  :repositories { "apache-releases" "http://repository.apache.org/content/repositories/releases/"}
  :plugins [[lein-ring "0.7.1"]
            [lein-marginalia "0.7.1"]]
  :ring {:handler rdflow.core/http-handler})
