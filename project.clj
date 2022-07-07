(defproject service-platform "0.1.0-SNAPSHOT"
  :description "Arrival Service Platform Test Task for Clojure Developer"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/clojurescript "1.11.4"]
                 [mount "0.1.16"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [compojure "1.6.2"]
                 [ring "1.9.5"]
                 [reagent "1.1.0"]
                 [re-frame "1.3.0-rc2"]
                 [day8.re-frame/http-fx "0.2.4"]
                 [day8.re-frame/re-frame-10x "1.2.2"]
                 [thheller/shadow-cljs "2.17.0"]
                 [ring/ring-defaults "0.3.3"]
                 [com.google.javascript/closure-compiler-unshaded "v20220104" :scope "provided"]
                 [cheshire "5.10.2"]
                 [funcool/struct "1.4.0"]
                 [tick "0.5.0-RC5"]
                 [cprop "0.1.19"]
                 [com.datomic/datomic-free "0.9.5697"]
                 [com.cognitect/transit-clj "0.8.319"]
                 [metosin/ring-middleware-format "0.6.0"]]
  :repl-options {:init-ns service-platform.core}
  :source-paths ["src/clj" "src/cljs" "src/cljc"]
  :resource-paths ["resources" "target/cljsbuild"]
  :main ^:skip-aot service-platform.core
  :plugins [[lein-shadow "0.4.0"]]
  :shadow-cljs
  {:nrepl        {:port 7002}
   :builds
   {:app
    {:target     :browser
     :output-dir "resources/public/app/js"
     :asset-path "app/js"
     :modules    {:main {:init-fn service-platform.core/init
                         :entries [service-platform.core]}}
     :devtools
     {:watch-dir "resources/public" :preloads [day8.re-frame-10x.preload]}
     :dev
     {:closure-defines {"re_frame.trace.trace_enabled_QMARK_"        true
                        "day8.re_frame.tracing.trace_enabled_QMARK_" true}
      :dependencies    [[day8.re-frame/re-frame-10x "1.2.2"]]}}
    :test
    {:target    :node-test
     :output-to "target/test/test.js"
     :autorun   true}}
   ;;:dev-http     {3000 "resources/public"}
   :npm-deps     []
   :npm-dev-deps [[xmlhttprequest "1.8.0"]]
   }
  :profiles {
             :uberjar {:omit-source  true
                       :prep-tasks   ["compile" ["shadow" "release" "app"]]
                       :aot          :all
                       :uberjar-name "testapp.jar"}
             :dev     {:jvm-opts       ["-Dconf=dev-config.edn"]
                       :resource-paths ["resources"]
                       :source-paths   ["test/clj" "test/cljs"]}
             :test    {:jvm-opts     ["-Dconf=test-config.edn"]
                       :source-paths ["test/clj" "test/cljs"]}})
