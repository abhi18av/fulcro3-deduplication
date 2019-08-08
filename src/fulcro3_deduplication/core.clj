(ns fulcro-deduplication.core
  (:import [org.apache.commons.text.similarity JaroWinklerDistance])
  (:require [clojure.java.io :as io]
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

(comment
  (first fulcro-src-paths)
  (count fulcro-src-paths))

;;;;;;;;

(defn get-file-class-path [a-file]
  (str "."
       (second
        (clojure.string/split
         (first
          (clojure.string/split a-file #":")) #"src"))))

(comment
  (get-file-class-path (nth fulcro-src-paths 1)))


;;;;;;;;


(def fulcro-class-paths
  (map get-file-class-path fulcro-src-paths))

(comment
  (first fulcro-class-paths))

;;;;;;;;

(defn defs-in-a-file [a-file]
  (mgrep/grep [#"defn"] (io/resource a-file)))

(defs-in-a-file (first fulcro-class-paths))

(comment
  (mgrep/grep [#"def " #"defn " #">defn " #"gw/>defn "] (io/resource "./fulcro/algorithms/merge.cljc"))
  (mgrep/grep [#"def"] (io/resource "./fulcro/algorithms/data_targeting.cljc"))
  (mgrep/grep #"def" (io/resource "./fulcro/algorithms/merge.cljc")))

;;;;;;;;

(def defs-in-all-files
  (flatten
   (map defs-in-a-file fulcro-class-paths)))

(comment
  (count defs-in-all-files)
  (first defs-in-all-files))

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
            (is-function-def? a-hashmap)) defs-in-all-files))

(comment
  (count all-defs-final)
  (first all-defs-final)
  (type
   (:file (first all-defs-final)))
  (second
   (clojure.string/split (.toString (:file (first all-defs-final))) #":")))

;;;;;;;
;; FIXME needs calibration for actual path of fulcro src
;; TODO refactor

(defn get-fulcro-src-file-name [a-java-net-url-object]
  (clojure.string/join "/"
                       (drop-while #(not= "src" %)
                                   (clojure.string/split
                                    (second
                                     (clojure.string/split (.toString (:file a-java-net-url-object)) #":")) #"/"))))

(comment
  (get-fulcro-src-file-name (first all-defs-final)))

;;;;;;;
;; NOTE need to normalize for < */?/! >

(defn get-normalized-function-name [a-java-net-url-object]
  (first
   (clojure.string/split
    (second
     (clojure.string/split (:line a-java-net-url-object) #" ")) #"\*|!|\?")))

(comment
  (get-normalized-function-name (first all-defs-final))
  (clojure.string/split "abcd*"  #"\*|!|\?")
  (clojure.string/split "abcd!"  #"\*|!|\?")
  (clojure.string/split "abcd?"  #"\*|!|\?"))

;;;;;;;

(defn are-similar-strings? [str-1 str-2]
  (let [score (.apply (JaroWinklerDistance.) str-1 str-2)]
    (if (> score 0.94)
      true
      false)))

(comment
  (are-similar-strings? "integrate-ident" "integrate-ident-abcd")
  (are-similar-strings? "integrate-ident" "integrate-ident?")
  (are-similar-strings? "integrate-ident" "integrate-ident!")
  (are-similar-strings? "integrate-ident" "integrate-ident*"))

;;;;;;;

(def all-unique-function-names
  (apply hash-set (map get-normalized-function-name all-defs-final)))

(comment
  (first all-unique-function-names)
  (count all-unique-function-names))

;;;;;;;

(doseq [keyval all-defs-final]
  (println keyval)
  #_(println (:line keyval)))

(zipmap (map keyword all-unique-function-names) (repeat (count all-unique-function-names) nil))

(comment
  (zipmap [:a :b :c] (repeat 3 nil)))

