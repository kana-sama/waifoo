(defproject waifoo "dev"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.439"]
                 [com.google.javascript/closure-compiler-unshaded "v20190121"] ;; for shadow-cljs
                 ;; tools
                 [thheller/shadow-cljs "2.7.24"]
                 [cider/cider-nrepl "0.20.0"]
                 ;; commmon
                 [org.clojure/tools.namespace "0.2.11"]
                 [org.clojure/core.match "0.3.0-alpha5"]
                 [com.taoensso/sente "1.14.0-RC2"]
                 [lynxeyes/dotenv "1.0.2"]
                 [inflections "0.13.1"]
                 [mount "0.1.16"]
                 ;; backend
                 [http-kit "2.3.0"]
                 [compojure "1.6.1"]
                 [ring "1.7.1"]
                 [ring-cors "0.1.12"]
                 ;; frontend
                 [reagent "0.8.1"]]
  :source-paths ["src/main"]
  :profiles {:dev {:source-paths ["src/dev"]}})
