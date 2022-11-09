(ns build
  (:require [clojure.edn :as edn]
            [clojure.tools.build.api :as b]))

(def project (-> (edn/read-string (slurp "deps.edn")) :aliases :neil :project))
(def lib (or (:name project) 'my/lib1))
(def version (or (:version project) "latest"))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def uber-file (format "target/%s-%s-standalone.jar" (name lib) version))

(defn clean [_]
  (b/delete {:path "target"}))

(defn uber
  "Builds the uberjar.
   
   Invoke this function with clojure -T:build uber"
  [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  ;; https://clojure.github.io/tools.build/clojure.tools.build.api.html#var-compile-clj
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir})
  ;; https://clojure.github.io/tools.build/clojure.tools.build.api.html#var-uber
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 'fosdem-dl.fosdem-dl}))
