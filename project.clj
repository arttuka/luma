(defproject luma "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [org.clojure/data.codec "0.1.1"]
                 [reagent "0.7.0"]
                 [re-frame "0.10.4"]
                 [secretary "1.2.3"]
                 [garden "1.3.3"]
                 [ns-tracker "0.3.1"]
                 [compojure "1.6.0"]
                 [yogthos/config "1.1"]
                 [ring "1.6.3"]
                 [ring/ring-defaults "0.3.1"]
                 [ring/ring-json "0.4.0"]
                 [http-kit "2.2.0"]]

  :plugins [[lein-cljsbuild "1.1.5"]
            [lein-garden "0.2.8"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "test/js"
                                    "resources/public/css"]

  :figwheel {:css-dirs ["resources/public/css"]
             :ring-handler luma.handler/handler}

  :garden {:builds [{:id           "screen"
                     :source-paths ["src/clj"]
                     :stylesheet   luma.css/screen
                     :compiler     {:output-to     "resources/public/css/screen.css"
                                    :pretty-print? true}}]}

  :aliases {"dev" ["do" "clean"
                        ["pdo" ["figwheel" "dev"]
                               ["garden" "auto"]]]
            "build" ["do" "clean"
                          ["cljsbuild" "once" "min"]
                          ["garden" "once"]]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.9.9"]
                   [re-frisk "0.5.3"]]

    :plugins      [[lein-figwheel "0.5.13"]
                   [lein-doo "0.1.8"]
                   [lein-pdo "0.1.1"]]
    :source-paths ["dev"]}
   :prod
   {:source-paths ["prod"]}}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "luma.core/mount-root"}
     :compiler     {:main                 luma.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload
                                           re-frisk.preload]
                    :external-config      {:devtools/config {:features-to-install :all}}
                    }}

    {:id           "min"
     :source-paths ["src/cljs"]
     :jar true
     :compiler     {:main            luma.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}

    {:id           "test"
     :source-paths ["src/cljs" "test/cljs"]
     :compiler     {:main          luma.runner
                    :output-to     "resources/public/js/compiled/test.js"
                    :output-dir    "resources/public/js/compiled/test/out"
                    :optimizations :none}}
    ]}

  :main luma.server

  :aot [luma.server]

  :uberjar-name "luma.jar"
  )
