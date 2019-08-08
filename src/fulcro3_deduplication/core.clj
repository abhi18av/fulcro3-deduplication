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

(def all-defs
  (mgrep/grep [#"defn"] (io/resource "example.clj")))

(comment
  (mgrep/grep [#"def " #"defn " #">defn " #"gw/>defn "] (io/resource "./fulcro/algorithms/merge.cljc"))
  (mgrep/grep [#"def"] (io/resource "./fulcro/algorithms/data_targeting.cljc"))
  (mgrep/grep #"def" (io/resource "./fulcro/algorithms/merge.cljc")))


;;;;;;;;

;; DONE drop the hashmaps which are not function defs

(defn is-function-def? [a-hashmap]
  (let [result (some #(re-matches #"\(defn||\(defn-||\(>defn||\(gw\/>defn" %)
                     (take 2
                           (clojure.string/split (:line a-hashmap) #" ")))]
    (if (= "" result)
      false
      true)))

(comment
  (is-function-def? (nth all-defs 3))
  (map is-function-def? all-defs)
  (some #(re-matches #"\(gw\/>defn" %) ["(gw/>defn"])
  (some #(re-matches #"\(defn|\(defn-" %) '("(defn" "(defn-" "(>defn")))


;;;;;;;;

(def all-defs-final
  (filter (fn [a-hashmap]
            (is-function-def? a-hashmap)) all-defs))

