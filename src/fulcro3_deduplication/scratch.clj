(ns fulcro3-deduplication.scratch
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [clojure.tools.reader :as tr]
            [multigrep.core  :as mgrep]
            [clojure.tools.reader.edn :as tredn]
            ;; fulcro namespaces
            [com.fulcrologic.fulcro.algorithms.misc :as misc]
            [com.fulcrologic.fulcro.dom :as dom]))

;;;;;;;;
;; from file system
;;;;;;;;

(def fulcro3-src-file-paths
  (let [fulcro3-src "./src/fulcro3_deduplication/fulcro3/src/main"
        ;fulcro3-src "resources/fulcro3/src/main/com/fulcrologic/fulcro"
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


;;;;;;;;;

(comment
  (nth
   (tredn/read-string
    (slurp (nth fulcro3-src-file-paths 12))) 3)

  (tr/read-string
   (slurp (nth fulcro3-src-file-paths 12))))

;;;;;;;;
;; multigrep based solution
;;;;;;;;

(def algorithm-merge
  (io/resource (nth fulcro3-src-file-paths 12)))

(mgrep/grep #"def" (io/resource "merge.txt"))

(mgrep/grep #"def" (io/resource "./fulcro/algorithms/merge.cljc"))

;;;;;;;;
;; from included namespaces
;; NOTE: in CLJ we can only involve the clj/cljc source
;;;;;;;;

(clojure.repl/dir com.fulcrologic.fulcro.algorithms.misc)
(clojure.repl/dir-fn com.fulcrologic.fulcro.algorithms.misc)
(clojure.repl/dir com.fulcrologic.fulcro.algorithms.merge)


;;;;;;;;
;; TODO: read the fulcro cljs files using the CLJS reader/analyzer
;;;;;;;;

(clojure.repl/dir 'fulcro3.src.main.com.fulcrologic.fulcro.dom)

