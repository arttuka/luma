(defproject luma "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/core.async "1.0.567"]
                 [org.clojure/tools.logging "1.0.0"]
                 [org.clojure/tools.reader "1.3.2"]
                 [compojure "1.6.1" :exclusions [ring/ring-core ring/ring-codec commons-codec]]
                 [yogthos/config "1.1.7"]
                 [ring/ring-core "1.8.0"]
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
                 [org.postgresql/postgresql "42.2.11"]
                 [hikari-cp "2.10.0"]
                 [clj-time "0.15.2"]]

  :plugins [[lein-ancient "0.6.15"]
            [lein-cljfmt "0.6.7"]
            [lein-kibit "0.1.8"]
            [no.terjedahl/lein-buster "0.2.0"]
            [jonase/eastwood "0.3.10"]]

  :min-lein-version "2.8.2"

  :source-paths ["src/clj" "src/cljc" "src/cljs" "reagent-util/src/cljs"]
  :test-paths []

  :clean-targets ^{:protect false} ["target"
                                    "resources/public/js"
                                    "resources/manifest.json"]

  :buster {:files       ["target/public/js/prod-main.js"]
           :files-base  "target/public"
           :output-base "resources/public"
           :manifest    "resources/manifest.json"}

  :cljfmt {:indents {async     [[:inner 0]]
                     when-let+ [[:inner 0]]}}

  :eastwood {:namespaces   [:source-paths]
             :config-files ["test-resources/eastwood.clj"]}

  :aliases {"fig"      ["trampoline" "run" "-m" "figwheel.main"]
            "fig:test" ["run" "-m" "figwheel.main" "-co" "test.cljs.edn" "-m" luma.figwheel-test-runner]
            "fig:min"  ["run" "-m" "figwheel.main" "-bo" "prod"]}

  :profiles {:dev      {:dependencies   [[binaryage/devtools "1.0.0"]
                                         [com.bhauman/rebel-readline-cljs "0.1.4" :exclusions [org.clojure/clojurescript]]
                                         [cider/piggieback "0.4.2" :exclusions [org.clojure/clojurescript]]
                                         [re-frisk "0.5.4.1" :exclusions [org.clojure/clojurescript]]
                                         [day8.re-frame/test "0.1.5"]
                                         [org.clojure/tools.namespace "1.0.0"]
                                         [ring/ring-devel "1.8.0"]]
                        :source-paths   ["dev"]
                        :test-paths     ["test/clj" "test/cljc" "test/cljs"]
                        :resource-paths ["dev-resources" "target"]}
             :provided {:dependencies [[org.clojure/clojurescript "1.10.597"]
                                       [com.bhauman/figwheel-main "0.2.3" :exclusions [org.clojure/clojurescript]]
                                       [com.google.errorprone/error_prone_annotations "2.3.4"]
                                       [com.google.code.findbugs/jsr305 "3.0.2"]
                                       [reagent "0.10.0"]
                                       [re-frame "0.12.0" :exclusions [org.clojure/clojurescript]]
                                       [arttuka/reagent-material-ui "4.9.5-1"]
                                       [cljsjs/react "16.13.0-0"]
                                       [cljsjs/react-dom "16.13.0-0"]
                                       [cljsjs/react-dom-server "16.13.0-0"]
                                       [com.cognitect/transit-cljs "0.8.256"]
                                       [com.andrewmcveigh/cljs-time "0.5.2"]]}
             :uberjar  {:dependencies [[com.fzakaria/slf4j-timbre "0.3.19"]]
                        :main         luma.main
                        :uberjar-name "luma.jar"
                        :auto-clean   false}}

  :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}
  :aot [luma.main])
