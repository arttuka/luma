(defproject luma "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/core.async "1.1.587"]
                 [org.clojure/tools.logging "1.1.0"]
                 [org.clojure/tools.reader "1.3.2"]
                 [compojure "1.6.1" :exclusions [ring/ring-core ring/ring-codec commons-codec]]
                 [yogthos/config "1.1.7"]
                 [ring/ring-core "1.8.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.5.0"]
                 [aleph "0.4.6"]
                 [byte-streams "0.2.4"]
                 [hiccup "1.0.5"]
                 [mount "0.1.16"]
                 [com.taoensso/sente "1.15.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.cognitect/transit-clj "1.0.324"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [org.postgresql/postgresql "42.2.12"]
                 [hikari-cp "2.11.0"]
                 [clj-time "0.15.2"]]

  :plugins [[lein-ancient "0.6.15"]
            [lein-cljfmt "0.6.7"]
            [lein-kibit "0.1.8"]
            [no.terjedahl/lein-buster "0.2.0"]
            [jonase/eastwood "0.3.11"]]

  :min-lein-version "2.8.2"

  :source-paths ["src/clj" "src/cljc" "src/cljs" "reagent-util/src/cljs"]
  :test-paths []

  :clean-targets ^{:protect false} ["target"
                                    "resources/public/js"
                                    "resources/manifest.json"
                                    ".shadow-cljs"]

  :buster {:files       ["target/public/js/main.js"]
           :files-base  "target/public"
           :output-base "resources/public"
           :manifest    "resources/manifest.json"}

  :cljfmt {:indents {async     [[:inner 0]]
                     when-let+ [[:inner 0]]}}

  :eastwood {:namespaces   [:source-paths]
             :config-files ["test-resources/eastwood.clj"]}

  :profiles {:dev     {:dependencies   [[binaryage/devtools "1.0.0"]
                                        [re-frisk "1.2.0" :exclusions [org.clojure/clojurescript]]
                                        [day8.re-frame/test "0.1.5"]
                                        [org.clojure/tools.namespace "1.0.0"]
                                        [ring/ring-devel "1.8.1"]]
                       :source-paths   ["dev"]
                       :test-paths     ["test/clj" "test/cljc" "test/cljs"]
                       :resource-paths ["dev-resources" "target"]}
             :provided {:dependencies [[reagent "0.10.0"]
                                       [re-frame "0.12.0" :exclusions [org.clojure/clojurescript]]
                                       [arttuka/reagent-material-ui "4.9.12-0"]
                                       [com.cognitect/transit-cljs "0.8.264"]
                                       [com.cognitect/transit-js "0.8.861"]
                                       [com.andrewmcveigh/cljs-time "0.5.2"]
                                       [thheller/shadow-cljsjs "0.0.21"]
                                       [thheller/shadow-cljs "2.8.109"]]}
             :uberjar {:dependencies [[com.fzakaria/slf4j-timbre "0.3.19"]]
                       :main         luma.main
                       :uberjar-name "luma.jar"
                       :auto-clean   false}}
  :aot [luma.main])
