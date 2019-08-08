(ns fulcro3-deduplication.core
  (:import [org.apache.commons.text.similarity JaroWinklerDistance])
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [multigrep.core  :as mgrep]))
;;;;;;;;

(def fulcro-src-paths
  (let [fulcro-src "./src/fulcro"
        file-extension-matcher (.getPathMatcher
                                (java.nio.file.FileSystems/getDefault)
                                "glob:*.{clj,cljs,cljc}")]
    (->> fulcro-src
         io/file
         file-seq
         (filter #(.isFile %))
         (filter #(.matches file-extension-matcher (.getFileName (.toPath %))))
         (mapv #(.getAbsolutePath %)))))

;;;;;;;;

(defn get-file-class-path [a-file]
  (str "."
       (second
        (clojure.string/split
         (first
          (clojure.string/split a-file #":")) #"src"))))

;;;;;;;;


(def fulcro-class-paths
  (map get-file-class-path fulcro-src-paths))


;;;;;;;;

(defn defs-in-a-file [a-file]
  (mgrep/grep [#"defn"] (io/resource a-file)))

;;;;;;;;

(def defs-in-all-files
  (flatten
   (map defs-in-a-file fulcro-class-paths)))

;;;;;;;;

(defn is-function-def? [a-hashmap]
  (let [result (some #(re-matches #"\(defn||\(defn-||\(>defn||\(gw\/>defn" %)
                     (take 2
                           (clojure.string/split (:line a-hashmap) #" ")))]
    (if (= "" result)
      false
      true)))

;;;;;;;;


(def all-defs-in-src
  (filter (fn [a-hashmap]
            (is-function-def? a-hashmap))
          defs-in-all-files))

;;;;;;;
;; TODO refactor

(defn get-fulcro-src-file-name [a-java-net-url-object]
  (clojure.string/join "/"
                       (drop-while #(not= "src" %)
                                   (clojure.string/split
                                    (second
                                     (clojure.string/split (.toString (:file a-java-net-url-object)) #":")) #"/"))))

;;;;;;;

(defn get-normalized-function-name [a-java-net-url-object]
  (first
   (clojure.string/split
    (second
     (clojure.string/split (:line a-java-net-url-object) #" ")) #"\*|!|\?")))

;;;;;;;

(defn are-similar-strings? [str-1 str-2]
  (let [score (.apply (JaroWinklerDistance.) str-1 str-2)]
    (if (> score 0.94)
      true
      false)))

;;;;;;;

(def all-unique-defs-sorted
  (sort
   (apply hash-set (map get-normalized-function-name all-defs-in-src))))

;;;;;;;

(def defs-frequency-map
  (zipmap (map keyword all-unique-defs-sorted) (repeat (count all-unique-defs-sorted) nil)))

;;;;;;;

(defn info-for-a-def [a-def]
   {:file-name (get-fulcro-src-file-name a-def)
    :function-name (get-normalized-function-name a-def)
    :line (:line a-def)
    :line-number (:line-number a-def)})

;;;;;;;

(def all-defs-info-final
  (map info-for-a-def all-defs-in-src))

;;;;;;;

(def frequencies-of-similar-defs
  (group-by :function-name all-defs-info-final))

;;;;;;;

(def possible-duplicates
  (filter (fn [a-freq-map]
             (if (< 1 (count (second a-freq-map)))
                 true
                 false))
         frequencies-of-similar-defs))



(clojure.pprint/pprint possible-duplicates (clojure.java.io/writer "possible-duplicates.edn"))
