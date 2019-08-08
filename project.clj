(defproject fulcro3-deduplication "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [clj-commons/multigrep "0.5.0"]
                 [org.apache.commons/commons-text "1.1"]
                 ;; experiments
                 [zcaudate/hara "2.8.7"]
                 [org.clojure/clojurescript "1.10.520"]
                 [com.fulcrologic/fulcro "3.0.0-alpha-21"]
                 [org.clojure/tools.reader "1.3.2"]]
  :repl-options {:init-ns fulcro3-deduplication.core})
