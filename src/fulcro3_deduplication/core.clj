(ns fulcro3-deduplication.core
  (:require [clojure.java.io :as io]
            [multigrep.core  :as mgrep]))

;;;;;;;;

(def fulcro3-src-file-paths
  (let [fulcro3-src "./src/fulcro"
        file-extension-matcher (.getPathMatcher
                                (java.nio.file.FileSystems/getDefault)
                                "glob:*.{clj,cljs,cljc}")]
    (->> fulcro3-src
         io/file
         file-seq
         (filter #(.isFile %))
         (filter #(.matches file-extension-matcher (.getFileName (.toPath %))))
         (mapv #(.getAbsolutePath %)))))

(comment
  (count fulcro3-src-file-paths)
  (first fulcro3-src-file-paths))

;;;;;;;;

(mgrep/grep [#"def " #"defn " #">defn " #"gw/>defn "] (io/resource "./fulcro/algorithms/merge.cljc"))

(comment
  (mgrep/grep #"def" (io/resource "./fulcro/algorithms/merge.cljc")))


;;;;;;;;
