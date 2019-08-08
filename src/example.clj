(ns example
 (:require [ghostwheel.core :as gw :refer [>defn =>]]
  [clojure.spec.alpha :as s]
  [clojure.set :as set]
  [com.fulcrologic.fulcro.algorithms.misc :as misc]
  [ghostwheel.core :as gw :refer [>defn =>]]
  [edn-query-language.core :as eql]))


(>defn integrate-ident
  "Integrate an ident into any number of places in the app state. This function is safe to use within mutation
  implementations as a general helper function.

  The named parameters can be specified any number of times. They are:

  - append:  A vector (path) to a list in your app state where this new object's ident should be appended. Will not append
  the ident if that ident is already in the list.
  - prepend: A vector (path) to a list in your app state where this new object's ident should be prepended. Will not place
  the ident if that ident is already in the list.
  - replace: A vector (path) to a specific location in app-state where this object's ident should be placed. Can target a to-one or to-many.
   If the target is a vector element then that element must already exist in the vector.

  NOTE: `ident` does not have to be an ident if you want to place denormalized data.  It can really be anything.

  Returns the updated state map."
  [state ident & named-parameters]
  [map? any? (s/* (s/or :path ::target :command #{:append :prepend :replace})) => map?]
  (let [actions (partition 2 named-parameters)]
    (reduce (fn [state [command data-path]]
              (let [already-has-ident-at-path? (fn [data-path] (some #(= % ident) (get-in state data-path)))]
                (case command
                  :prepend (if (already-has-ident-at-path? data-path)
                             state
                             (do
                               (assert (vector? (get-in state data-path)) (str "Path " data-path " for prepend must target an app-state vector."))
                               (update-in state data-path #(into [ident] %))))
                  :append (if (already-has-ident-at-path? data-path)
                            state
                            (do
                              (assert (vector? (get-in state data-path)) (str "Path " data-path " for append must target an app-state vector."))
                              (update-in state data-path conj ident)))
                  :replace (let [path-to-vector (butlast data-path)
                                 to-many?       (and (seq path-to-vector) (vector? (get-in state path-to-vector)))
                                 index          (last data-path)
                                 vector         (get-in state path-to-vector)]
                             (assert (vector? data-path) (str "Replacement path must be a vector. You passed: " data-path))
                             (when to-many?
                               (do
                                 (assert (vector? vector) "Path for replacement must be a vector")
                                 (assert (number? index) "Path for replacement must end in a vector index")
                                 (assert (contains? vector index) (str "Target vector for replacement does not have an item at index " index))))
                             (assoc-in state data-path ident))
                  (throw (ex-info "Unknown post-op to merge-state!: " {:command command :arg data-path})))))
      state actions)))


(gw/>defn integrate-ident
  "Integrate an ident into any number of places in the app state. This function is safe to use within mutation
  implementations as a general helper function.

  The named parameters can be specified any number of times. They are:

  - append:  A vector (path) to a list in your app state where this new object's ident should be appended. Will not append
  the ident if that ident is already in the list.
  - prepend: A vector (path) to a list in your app state where this new object's ident should be prepended. Will not place
  the ident if that ident is already in the list.
  - replace: A vector (path) to a specific location in app-state where this object's ident should be placed. Can target a to-one or to-many.
   If the target is a vector element then that element must already exist in the vector.

  NOTE: `ident` does not have to be an ident if you want to place denormalized data.  It can really be anything.

  Returns the updated state map."
  [state ident & named-parameters]
  [map? any? (s/* (s/or :path ::target :command #{:append :prepend :replace})) => map?]
  (let [actions (partition 2 named-parameters)]
    (reduce (fn [state [command data-path]]
              (let [already-has-ident-at-path? (fn [data-path] (some #(= % ident) (get-in state data-path)))]
                (case command
                  :prepend (if (already-has-ident-at-path? data-path)
                             state
                             (do
                               (assert (vector? (get-in state data-path)) (str "Path " data-path " for prepend must target an app-state vector."))
                               (update-in state data-path #(into [ident] %))))
                  :append (if (already-has-ident-at-path? data-path)
                            state
                            (do
                              (assert (vector? (get-in state data-path)) (str "Path " data-path " for append must target an app-state vector."))
                              (update-in state data-path conj ident)))
                  :replace (let [path-to-vector (butlast data-path)
                                 to-many?       (and (seq path-to-vector) (vector? (get-in state path-to-vector)))
                                 index          (last data-path)
                                 vector         (get-in state path-to-vector)]
                             (assert (vector? data-path) (str "Replacement path must be a vector. You passed: " data-path))
                             (when to-many?
                               (do
                                 (assert (vector? vector) "Path for replacement must be a vector")
                                 (assert (number? index) "Path for replacement must end in a vector index")
                                 (assert (contains? vector index) (str "Target vector for replacement does not have an item at index " index))))
                             (assoc-in state data-path ident))
                  (throw (ex-info "Unknown post-op to merge-state!: " {:command command :arg data-path})))))
      state actions)))





(defn integrate-ident*
  "Integrate an ident into any number of places in the app state. This function is safe to use within mutation
  implementations as a general helper function.

  The named parameters can be specified any number of times. They are:

  - append:  A vector (path) to a list in your app state where this new object's ident should be appended. Will not append
  the ident if that ident is already in the list.
  - prepend: A vector (path) to a list in your app state where this new object's ident should be prepended. Will not append
  the ident if that ident is already in the list.
  - replace: A vector (path) to a specific location in app-state where this object's ident should be placed. Can target a to-one or to-many.
   If the target is a vector element then that element must already exist in the vector."
  [state ident & named-parameters]
  (let [actions (partition 2 named-parameters)]
    (reduce (fn [state [command data-path]]
              (let [already-has-ident-at-path? (fn [data-path] (some #(= % ident) (get-in state data-path)))]
                (case command
                  :prepend (if (already-has-ident-at-path? data-path)
                             state
                             (update-in state data-path #(into [ident] %)))
                  :append (if (already-has-ident-at-path? data-path)
                            state
                            (update-in state data-path (fnil conj []) ident))
                  :replace (let [path-to-vector (butlast data-path)
                                 to-many?       (and (seq path-to-vector) (vector? (get-in state path-to-vector)))
                                 index          (last data-path)
                                 vector         (get-in state path-to-vector)]
                             (when-not (vector? data-path) (log/error "Replacement path must be a vector. You passed: " data-path))
                             (when to-many?
                               (cond
                                 (not (vector? vector)) (log/error "Path for replacement must be a vector")
                                 (not (number? index)) (log/error "Path for replacement must end in a vector index")
                                 (not (contains? vector index)) (log/error "Target vector for replacement does not have an item at index " index)))
                             (assoc-in state data-path ident))
                  state)))
      state actions)))


(defn- integrate-ident
  "Integrate an ident into any number of aliases in the state machine.
  Aliases must point to a list of idents.

  The named parameters can be specified any number of times. They are:

  - append:  A keyword (alias) to a list in your app state where this new object's ident should be appended. Will not append
  the ident if that ident is already in the list.
  - prepend: A keyword (alias) to a list in your app state where this new object's ident should be prepended. Will not append
  the ident if that ident is already in the list."
  [env ident & named-parameters]
  [::env eql/ident? (s/* (s/cat :name #{:prepend :append} :param keyword?)) => ::env]
  (log/debug "Integrating" ident "on" (::asm-id env))
  (let [actions (partition 2 named-parameters)]
    (reduce (fn [env [command alias-to-idents]]
              (let [alias-value                 (alias-value env alias-to-idents)
                    already-has-ident-at-alias? (some #(= % ident) alias-value)]
                (case command
                  :prepend (if already-has-ident-at-alias?
                             env
                             (update-aliased env alias-to-idents #(into [ident] %)))
                  :append (if already-has-ident-at-alias?
                            env
                            (update-aliased env alias-to-idents (fnil conj []) ident))
                  (throw (ex-info "Unknown operation for integrate-ident: " {:command command :arg alias-to-idents})))))
      env actions)))



(defn- normalize* [query data refs union-seen transform]
  (let [data (if (and transform (not (vector? data)))
               (transform query data)
               data)]
    (cond
      (= '[*] query) data

      ;; union case
      (map? query)
      (let [class (-> query meta :component)
            ident (get-ident class data)]
        (if-not (nil? ident)
          (vary-meta (normalize* (get query (first ident)) data refs union-seen transform)
            assoc ::tag (first ident))                      ; FIXME: What is tag for?
          (throw (ex-info "Union components must have an ident" {}))))

      (vector? data) data                                   ;; already normalized

      :else
      (loop [q (seq query) ret data]
        (if-not (nil? q)
          (let [expr (first q)]
            (if (util/join? expr)
              (let [[k sel] (util/join-entry expr)
                    recursive?  (util/recursion? sel)
                    union-entry (if (util/union? expr) sel union-seen)
                    sel         (if recursive?
                                  (if-not (nil? union-seen)
                                    union-seen
                                    query)
                                  sel)
                    class       (-> sel meta :component)
                    v           (get data k)]
                (cond
                  ;; graph loop: db->tree leaves ident in place
                  (and recursive? (eql/ident? v)) (recur (next q) ret)
                  ;; normalize one
                  (map? v)
                  (let [x (normalize* sel v refs union-entry transform)]
                    (if-not (or (nil? class) (not (has-ident? class)))
                      (let [i (get-ident class x)]
                        (swap! refs update-in [(first i) (second i)] merge x)
                        (recur (next q) (assoc ret k i)))
                      (recur (next q) (assoc ret k x))))

                  ;; normalize many
                  (and (vector? v) (not (eql/ident? v)) (not (eql/ident? (first v))))
                  (let [xs (into [] (map #(normalize* sel % refs union-entry transform)) v)]
                    (if-not (or (nil? class) (not (has-ident? class)))
                      (let [is (into [] (map #(get-ident class %)) xs)]
                        (if (vector? sel)
                          (when-not (empty? is)
                            (swap! refs
                              (fn [refs]
                                (reduce (fn [m [i x]]
                                          (update-in m i merge x))
                                  refs (zipmap is xs)))))
                          ;; union case
                          (swap! refs
                            (fn [refs']
                              (reduce
                                (fn [ret [i x]]
                                  (update-in ret i merge x))
                                refs' (map vector is xs)))))
                        (recur (next q) (assoc ret k is)))
                      (recur (next q) (assoc ret k xs))))

                  ;; missing key
                  (nil? v)
                  (recur (next q) ret)

                  ;; can't handle
                  :else (recur (next q) (assoc ret k v))))
              (let [k (if (seq? expr) (first expr) expr)
                    v (get data k)]
                (if (nil? v)
                  (recur (next q) ret)
                  (recur (next q) (assoc ret k v))))))
          ret)))))

(defn tree->db
  "Given a component class or instance and a tree of data, use the component's
   query to transform the tree into the default database format. All nodes that
   can be mapped via Ident implementations wil be replaced with ident links. The
   original node data will be moved into tables indexed by ident. If merge-idents
   option is true, will return these tables in the result instead of as metadata."
  ([x data]
   (tree->db x data false))
  ([x data #?(:clj merge-idents :cljs ^boolean merge-idents)]
   (tree->db x data merge-idents nil))
  ([x data #?(:clj merge-idents :cljs ^boolean merge-idents) transform]
   (let [refs (atom {})
         x    (if (vector? x) x (get-query x data))
         ret  (normalize* x data refs nil transform)]
     (if merge-idents
       (let [refs' @refs] (merge ret refs'))
       (with-meta ret @refs)))))
