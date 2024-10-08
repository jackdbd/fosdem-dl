(ns build
  (:require
   [clojure.edn :as edn]
   [clojure.string :as str]
   [clojure.tools.build.api :as b]))

;; https://clojure.github.io/tools.build/clojure.tools.build.api.html

(def project (-> (edn/read-string (slurp "deps.edn")) :aliases :neil :project))
(def splits (str/split (str (:name project)) (re-pattern "/")))
#_(def group-id (first splits))
(def app-id (last splits))
(def app-version (or (:version project) "latest"))
;; (def lib (:name project))
;; (def lib 'fosdem-dl)
;; (prn "group-id" group-id)
;; (prn "app-id" app-id)
;; (prn "lib" lib)

(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def uber-file (format "target/%s-%s-standalone.jar" app-id app-version))

(defn clean "Remove all compilation artifacts." [_]
  (b/delete {:path "target"}))

(defn compile-clj [_]
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir}))

(defn uber "Build the uber-JAR." [_]
  (println (format "\nCopying resources (including pods) to %s ..." class-dir))
  (b/copy-dir {:src-dirs ["resources"]
               :target-dir class-dir})

  ;; (compile-clj nil)

  (println "\nBuilding" uber-file "...")
  (b/uber {:basis basis
           :class-dir class-dir
           :main 'fosdem-dl.cli
           :uber-file uber-file}))
