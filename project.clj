(defproject luma "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/core.async "0.4.500"]
                 [org.clojure/tools.logging "0.4.1"]
                 [org.clojure/tools.reader "1.3.2"]
                 [compojure "1.6.1" :exclusions [ring/ring-core ring/ring-codec commons-codec]]
                 [yogthos/config "1.1.4"]
                 [ring/ring-core "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.4.0" :exclusions [com.fasterxml.jackson.core/jackson-core]]
                 [aleph "0.4.6"]
                 [byte-streams "0.2.4"]
                 [hiccup "1.0.5"]
                 [mount "0.1.16"]
                 [com.taoensso/sente "1.14.0-RC2"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.cognitect/transit-clj "0.8.313"]
                 [org.clojure/java.jdbc "0.7.9"]
                 [org.postgresql/postgresql "42.2.6"]
                 [hikari-cp "2.7.1"]
                 [clj-time "0.15.1"]]

  :plugins [[lein-ancient "0.6.15"]
            [lein-asset-minifier "0.4.6"]
            [lein-cljfmt "0.6.4"]
            [lein-garden "0.3.0" :exclusions [org.apache.commons/commons-compress]]
            [no.terjedahl/lein-buster "0.2.0"]
            [jonase/eastwood "0.3.5"]]

  :min-lein-version "2.8.2"

  :source-paths ["src/clj" "src/cljc" "src/cljs"]
  :test-paths []

  :clean-targets ^{:protect false} ["target"
                                    "resources/public/js"
                                    "resources/public/css"
                                    "resources/manifest.json"]

  :garden {:builds [{:id           "screen"
                     :source-paths ["src/clj" "src/cljc"]
                     :stylesheet   luma.styles.main/screen
                     :compiler     {:output-to     "target/public/css/screen.css"
                                    :pretty-print? true}}]}
  :minify-assets [[:css {:source "target/public/css/screen.css"
                         :target "target/public/css/screen.min.css"}]]

  :buster {:files       ["target/public/js/prod-main.js"
                         "target/public/css/screen.min.css"]
           :files-base  "target/public"
           :output-base "resources/public"
           :manifest    "resources/manifest.json"}

  :cljfmt {:indents {async [[:inner 0]]}}

  :aliases {"fig"      ["trampoline" "run" "-m" "figwheel.main"]
            "fig:test" ["run" "-m" "figwheel.main" "-co" "test.cljs.edn" "-m" luma.figwheel-test-runner]
            "fig:min"  ["run" "-m" "figwheel.main" "-bo" "prod"]}

  :profiles {:dev      {:dependencies   [[binaryage/devtools "0.9.10"]
                                         [com.bhauman/rebel-readline-cljs "0.1.4" :exclusions [org.clojure/clojurescript]]
                                         [cider/piggieback "0.4.1" :exclusions [org.clojure/clojurescript]]
                                         [hawk "0.2.11"]
                                         [re-frisk "0.5.4.1" :exclusions [org.clojure/clojurescript]]
                                         [day8.re-frame/test "0.1.5"]
                                         [org.clojure/tools.namespace "0.3.0"]
                                         [garden "1.3.9"]
                                         [ring/ring-devel "1.7.1"]]
                        :source-paths   ["dev"]
                        :test-paths     ["test/clj" "test/cljc" "test/cljs"]
                        :resource-paths ["dev-resources" "target"]
                        :eastwood       {:namespaces   [:source-paths :test-paths]
                                         :config-files ["dev-resources/eastwood.clj"]}}
             :provided {:dependencies [[org.clojure/clojurescript "1.10.520"]
                                       [com.bhauman/figwheel-main "0.2.1" :exclusions [org.clojure/clojurescript]]
                                       [com.google.errorprone/error_prone_annotations "2.3.3"]
                                       [com.google.code.findbugs/jsr305 "3.0.2"]
                                       [reagent "0.8.1"]
                                       [re-frame "0.10.7" :exclusions [org.clojure/clojurescript]]
                                       [cljs-react-material-ui "0.2.50" :exclusions [org.clojure/clojurescript]]
                                       [cljsjs/react "16.8.6-0"]
                                       [cljsjs/react-dom "16.8.6-0"]
                                       [cljsjs/react-dom-server "16.8.6-0"]
                                       [cljsjs/react-autosuggest "9.4.3-0"]
                                       [binaryage/oops "0.7.0"]
                                       [garden "1.3.9"]
                                       [com.cognitect/transit-cljs "0.8.256"]
                                       [com.andrewmcveigh/cljs-time "0.5.2"]]}
             :uberjar  {:dependencies [[com.fzakaria/slf4j-timbre "0.3.13"]]
                        :main         luma.main
                        :uberjar-name "luma.jar"
                        :auto-clean   false}}

  :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}
  :aot [luma.main])
