(defproject luma "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/core.async "0.4.500"]
                 [org.clojure/tools.logging "0.5.0"]
                 [org.clojure/tools.reader "1.3.2"]
                 [compojure "1.6.1" :exclusions [ring/ring-core ring/ring-codec commons-codec]]
                 [yogthos/config "1.1.6"]
                 [ring/ring-core "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.5.0"]
                 [aleph "0.4.6"]
                 [byte-streams "0.2.4"]
                 [hiccup "1.0.5"]
                 [mount "0.1.16"]
                 [com.taoensso/sente "1.14.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.cognitect/transit-clj "0.8.319"]
                 [org.clojure/java.jdbc "0.7.10"]
                 [org.postgresql/postgresql "42.2.8"]
                 [hikari-cp "2.9.0"]
                 [clj-time "0.15.2"]]

  :plugins [[lein-ancient "0.6.15"]
            [lein-cljfmt "0.6.4"]
            [lein-kibit "0.1.7"]
            [no.terjedahl/lein-buster "0.2.0"]
            [jonase/eastwood "0.3.6"]]

  :min-lein-version "2.8.2"

  :source-paths ["src/clj" "src/cljc" "src/cljs"]
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

  :profiles {:dev      {:dependencies   [[binaryage/devtools "0.9.10"]
                                         [com.bhauman/rebel-readline-cljs "0.1.4" :exclusions [org.clojure/clojurescript]]
                                         [cider/piggieback "0.4.2" :exclusions [org.clojure/clojurescript]]
                                         [re-frisk "0.5.4.1" :exclusions [org.clojure/clojurescript]]
                                         [day8.re-frame/test "0.1.5"]
                                         [org.clojure/tools.namespace "0.3.1"]
                                         [ring/ring-devel "1.7.1"]]
                        :source-paths   ["dev"]
                        :test-paths     ["test/clj" "test/cljc" "test/cljs"]
                        :resource-paths ["dev-resources" "target"]}
             :provided {:dependencies [[org.clojure/clojurescript "1.10.520"]
                                       [com.bhauman/figwheel-main "0.2.3" :exclusions [org.clojure/clojurescript]]
                                       [com.google.errorprone/error_prone_annotations "2.3.3"]
                                       [com.google.code.findbugs/jsr305 "3.0.2"]
                                       [reagent "0.9.0-rc2"]
                                       [re-frame "0.10.9" :exclusions [org.clojure/clojurescript]]
                                       [arttuka/reagent-material-ui "4.6.0-0"]
                                       [cljsjs/react "16.11.0-0"]
                                       [cljsjs/react-dom "16.11.0-0"]
                                       [cljsjs/react-dom-server "16.11.0-0"]
                                       [com.cognitect/transit-cljs "0.8.256"]
                                       [com.andrewmcveigh/cljs-time "0.5.2"]]}
             :uberjar  {:dependencies [[com.fzakaria/slf4j-timbre "0.3.14"]]
                        :main         luma.main
                        :uberjar-name "luma.jar"
                        :auto-clean   false}}

  :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}
  :aot [luma.main])
