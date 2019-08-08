(ns fulcro3-deduplication.core
 (:require [clojure.java.io :as io]
           [clojure.tools.reader.edn :as tredn]))

;;;;;;;;


(def fulcro3-src-file-paths
(let [fulcro3-src "resources/fulcro3/src/main/com/fulcrologic/fulcro"
      grammar-matcher (.getPathMatcher
                       (java.nio.file.FileSystems/getDefault)
                       "glob:*.{clj,cljs,cljc}")]
  (->> fulcro3-src
       io/file
       file-seq
       (filter #(.isFile %))
       (filter #(.matches grammar-matcher (.getFileName (.toPath %))))
       (mapv #(.getAbsolutePath %)))))

(comment
(count fulcro3-src-file-paths)
(first fulcro3-src-file-paths))

