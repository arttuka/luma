(defproject luma "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/core.async "1.3.618"]
                 [org.clojure/tools.logging "1.1.0"]
                 [org.clojure/tools.reader "1.3.6"]
                 [compojure "1.6.2"]
                 [yogthos/config "1.1.8"]
                 [ring/ring-core "1.9.4"]
                 [ring/ring-defaults "0.3.3"]
                 [ring/ring-json "0.5.1"]
                 [aleph "0.4.6"]
                 [byte-streams "0.2.4"]
                 [hiccup "1.0.5"]
                 [mount "0.1.16"]
                 [com.taoensso/sente "1.16.2"]
                 [com.taoensso/timbre "5.1.2"]
                 [com.cognitect/transit-clj "1.0.324" :exclusions [javax.xml.bind/jaxb-api]]
                 [org.clojure/java.jdbc "0.7.12"]
                 [org.postgresql/postgresql "42.2.23"]
                 [hikari-cp "2.13.0"]
                 [clj-time "0.15.2"]]

  :plugins [[lein-ancient "0.7.0"]
            [lein-cljfmt "0.8.0"]
            [lein-kibit "0.1.8"]
            [jonase/eastwood "0.9.9"]]

  :min-lein-version "2.8.2"

  :source-paths ["src/clj" "src/cljc" "src/cljs"]
  :test-paths []

  :clean-targets ^{:protect false} ["target"
                                    "resources/public/js"
                                    "resources/manifest.edn"
                                    ".shadow-cljs"]

  :cljfmt {:indents {async           [[:inner 0]]
                     when-let+       [[:inner 0]]
                     react-component [[:inner 0]]}}

  :eastwood {:namespaces   [:source-paths]
             :config-files ["test-resources/eastwood.clj"]}

  :profiles {:dev      {:dependencies   [[binaryage/devtools "1.0.3"]
                                         [re-frisk "1.5.1" :exclusions [org.clojure/clojurescript]]
                                         [day8.re-frame/test "0.1.5"]
                                         [org.clojure/tools.namespace "1.1.0"]
                                         [ring/ring-devel "1.9.4"]]
                        :source-paths   ["dev"]
                        :test-paths     ["test/clj" "test/cljc" "test/cljs"]
                        :resource-paths ["dev-resources" "target"]}
             :provided {:dependencies [[reagent "1.1.0"]
                                       [re-frame "1.2.0"]
                                       [arttuka/reagent-material-ui "5.0.0-beta.5-0"]
                                       [com.cognitect/transit-cljs "0.8.269"]
                                       [com.cognitect/transit-js "0.8.874"]
                                       [com.andrewmcveigh/cljs-time "0.5.2"]
                                       [thheller/shadow-cljs "2.15.6"]]}
             :uberjar  {:dependencies [[com.fzakaria/slf4j-timbre "0.3.21"]]
                        :main         luma.main
                        :uberjar-name "luma.jar"
                        :auto-clean   false}}
  :aot [luma.main])
