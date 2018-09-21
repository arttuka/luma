(defproject luma "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.4.474"]
                 [org.clojure/clojurescript "1.10.238"]
                 [org.clojure/data.codec "0.1.1"]
                 [org.clojure/tools.logging "0.4.0"]
                 [reagent "0.7.0"]
                 [re-frame "0.10.5" :exclusions [org.clojure/tools.logging]]
                 [cljs-react-material-ui "0.2.50"]
                 [cljsjs/react "16.3.0-0"]
                 [cljsjs/react-dom "16.3.0-0"]
                 [cljsjs/react-autosuggest "9.3.2-0"]
                 [binaryage/oops "0.5.8"]
                 [garden "1.3.5"]
                 [compojure "1.6.0" :exclusions [ring/ring-core commons-codec]]
                 [yogthos/config "1.1.1"]
                 [ring "1.6.3"]
                 [ring/ring-defaults "0.3.1"]
                 [ring/ring-json "0.4.0" :exclusions [com.fasterxml.jackson.core/jackson-core]]
                 [http-kit "2.2.0"]
                 [mount "0.1.12"]
                 [com.taoensso/sente "1.12.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.cognitect/transit-clj "0.8.303"]
                 [com.cognitect/transit-cljs "0.8.256"]
                 [org.clojure/java.jdbc "0.7.5"]
                 [org.postgresql/postgresql "42.2.2"]
                 [hikari-cp "2.2.0"]
                 [clj-time "0.14.2"]]

  :plugins [[lein-cljsbuild "1.1.5"]
            [lein-figwheel "0.5.14"]
            [lein-garden "0.2.8" :exclusions [org.apache.commons/commons-compress]]]

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

  :profiles {:dev     {:dependencies   [[binaryage/devtools "0.9.9"]
                                        [figwheel-sidecar "0.5.15" :exclusions [org.clojure/tools.nrepl]]
                                        [com.cemerick/piggieback "0.2.2"]
                                        [hawk "0.2.11"]
                                        [re-frisk "0.5.4"]
                                        [day8.re-frame/test "0.1.5"]
                                        [org.clojure/tools.namespace "0.2.11"]]

                       :plugins        [[lein-figwheel "0.5.15"]
                                        [lein-doo "0.1.8" :exclusions [org.clojure/tools.reader]]
                                        [lein-pdo "0.1.1"]]
                       :source-paths   ["dev" "test/clj" "test/cljc"]
                       :resource-paths ["dev-resources"]}
             :uberjar {:source-paths ["prod"]
                       :prep-tasks   ["compile"
                                      ["cljsbuild" "once" "min"]
                                      ["garden" "once"]]}}

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :cljsbuild {:builds [{:id           "dev"
                        :source-paths ["src/cljs" "src/cljc"]
                        :figwheel     {:on-jsload "luma.core/mount-root"}
                        :compiler     {:main                 luma.core
                                       :output-to            "resources/public/js/compiled/app.js"
                                       :output-dir           "resources/public/js/compiled/out"
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
                                       :optimizations   :whitespace
                                       :closure-defines {goog.DEBUG false}
                                       :pretty-print    false}}

                       {:id           "test"
                        :source-paths ["src/cljs" "src/cljc" "test/cljs" "test/cljc"]
                        :compiler     {:main          luma.runner
                                       :output-to     "resources/public/js/compiled/test.js"
                                       :output-dir    "resources/public/js/compiled/test/out"
                                       :optimizations :none}}]}

  :main luma.main
  :aot [luma.main]
  :uberjar-name "luma.jar")
