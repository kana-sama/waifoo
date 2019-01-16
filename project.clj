(defproject waifoo "dev"
  :min-lein-version "2.8.3"

  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.439"]
                 ;; commmon
                 [org.clojure/core.async "0.4.490"]
                 ;; clj
                 [compojure "1.6.1"]
                 [ring/ring-core "1.7.1"]
                 [ring-cors "0.1.12"]
                 ;; cljs
                 [reagent "0.8.1"]
                 [cljs-http "0.1.45"]]

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-figwheel "0.5.16"]
            [lein-cooper "1.2.2"]
            [lein-ring "0.12.4"]]
            
  :main waifoo.core
  :source-paths ["src/clj"]
  :resource-paths ["resources"]

  :cljsbuild {:builds [{:id "dev"
                        :compiler {:asset-path "js/out"
                                   :main "waifoo.core"
                                   :optimizations :none
                                   :output-to "resources/public/js/index.js"
                                   :output-dir "resources/public/js/out"}
                        :figwheel {:websocket-host :js-client-host
                                   :on-jsload "waifoo.core/run"}
                        :source-paths ["src/cljs"]}]}

  :ring {:auto-reload? true
         :handler waifoo.core/app
         :reload-paths ["src/clj" "resources/"]
         :port 3001}

  :figwheel {:css-dirs ["resources/public/css"]
             :server-port 3000}

  :cooper {"frontend"  ["lein" "figwheel"]
           "backend" ["lein" "ring" "server-headless"]})

