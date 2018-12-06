(defproject luma "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.4.490"]
                 [org.clojure/tools.logging "0.4.1"]
                 [org.clojure/tools.reader "1.3.2"]
                 [compojure "1.6.1" :exclusions [ring/ring-core ring/ring-codec commons-codec]]
                 [yogthos/config "1.1.1"]
                 [ring/ring-core "1.7.1" :exclusions []]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.4.0" :exclusions [com.fasterxml.jackson.core/jackson-core]]
                 [http-kit "2.3.0"]
                 [mount "0.1.15"]
                 [com.taoensso/sente "1.13.1"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.fzakaria/slf4j-timbre "0.3.12"]
                 [com.cognitect/transit-clj "0.8.313"]
                 [org.clojure/java.jdbc "0.7.8"]
                 [org.postgresql/postgresql "42.2.5"]
                 [hikari-cp "2.6.0"]
                 [clj-time "0.15.1"]]

  :plugins [[lein-cljsbuild "1.1.5"]
            [lein-figwheel "0.5.15"]
            [lein-garden "0.3.0" :exclusions [org.apache.commons/commons-compress]]
            [lein-environ "1.1.0" :exclusions [org.clojure/clojure]]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj" "src/cljc"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "test/js"
                                    "resources/public/css"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :garden {:builds [{:id           "screen"
                     :source-paths ["src/clj"]
                     :stylesheet   luma.styles.main/screen
                     :compiler     {:output-to     "resources/public/css/screen.css"
                                    :pretty-print? true}}]}

  :aliases {"dev"   ["do" "clean"
                     ["pdo" ["figwheel" "dev"]
                      ["garden" "auto"]]]
            "build" ["do" "clean"
                     ["cljsbuild" "once" "min"]
                     ["garden" "once"]]}

  :profiles {:dev      {:env            {:dev true}
                        :dependencies   [[binaryage/devtools "0.9.10"]
                                         [figwheel-sidecar "0.5.17" :exclusions [org.clojure/tools.nrepl args4j]]
                                         [com.cemerick/piggieback "0.2.2"]
                                         [hawk "0.2.11"]
                                         [re-frisk "0.5.4" :exclusions [args4j]]
                                         [day8.re-frame/test "0.1.5"]
                                         [org.clojure/tools.namespace "0.2.11"]
                                         [garden "1.3.6"]
                                         [ring/ring-devel "1.7.1" :exclusions []]]

                        :plugins        [[lein-doo "0.1.8" :exclusions [org.clojure/tools.reader]]
                                         [lein-pdo "0.1.1"]]
                        :source-paths   ["dev" "test/clj" "test/cljc"]
                        :resource-paths ["dev-resources"]}
             :provided {:dependencies [[org.clojure/clojurescript "1.10.439" :exclusions [org.clojure/tools.reader]]
                                       [com.google.errorprone/error_prone_annotations "2.1.3"]
                                       [com.google.code.findbugs/jsr305 "3.0.2"]
                                       [reagent "0.8.1"]
                                       [re-frame "0.10.6" :exclusions [org.clojure/tools.logging args4j]]
                                       [cljs-react-material-ui "0.2.50" :exclusions [args4j]]
                                       [cljsjs/react "16.6.0-0"]
                                       [cljsjs/react-dom "16.6.0-0"]
                                       [cljsjs/react-autosuggest "9.3.4-0"]
                                       [binaryage/oops "0.6.3"]
                                       [garden "1.3.6"]
                                       [com.cognitect/transit-cljs "0.8.256"]]}
             :uberjar  {:prep-tasks ["compile"
                                     ["cljsbuild" "once" "min"]
                                     ["garden" "once"]]
                        :main       luma.main}}

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :cljsbuild {:builds [{:id           "dev"
                        :source-paths ["src/cljs" "src/cljc"]
                        :figwheel     {:on-jsload "luma.core/mount-root"}
                        :compiler     {:main                 luma.core
                                       :output-to            "resources/public/js/compiled/app.js"
                                       :asset-path           "js/compiled/out"
                                       :source-map-timestamp true
                                       :preloads             [devtools.preload
                                                              re-frisk.preload]
                                       :external-config      {:devtools/config {:features-to-install :all}}
                                       :optimizations        :none}}

                       {:id           "min"
                        :source-paths ["src/cljs" "src/cljc"]
                        :jar          true
                        :compiler     {:main            luma.core
                                       :output-to       "resources/public/js/compiled/app.js"
                                       :output-dir      "resources/public/js/compiled"
                                       :optimizations   :advanced
                                       :closure-defines {goog.DEBUG false}
                                       :pretty-print    false}}

                       {:id           "test"
                        :source-paths ["src/cljs" "src/cljc" "test/cljs" "test/cljc"]
                        :compiler     {:main          luma.runner
                                       :output-to     "resources/public/js/compiled/test.js"
                                       :output-dir    "resources/public/js/compiled/test/out"
                                       :optimizations :none}}]}
  :aot [luma.main]
  :uberjar-name "luma.jar")
