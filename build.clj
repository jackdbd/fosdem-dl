(ns build
  (:require [clojure.edn :as edn]
            [clojure.tools.build.api :as b]))

(def project (-> (edn/read-string (slurp "deps.edn")) :aliases :neil :project))
(def lib (:name project))

(def version (or (:version project) "latest"))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def uber-file (format "target/%s-%s-standalone.jar" (name lib) version))

(defn clean "Remove all compilation artifacts." [_]
  (b/delete {:path "target"}))

;; https://clojure.github.io/tools.build/clojure.tools.build.api.html#var-write-pom
(defn- pom-template [{:keys [version]}]
  [[:description "CLI to download the talks given at FOSDEM over the years."]
   [:url "https://github.com/jackdbd/fosdem-dl"]
   [:licenses
    [:license
     [:name "The MIT License"]
     ;; https://www.tldrlegal.com/license/mit-license
     [:url "https://opensource.org/license/MIT"]]]
   [:developers
    [:developer
     [:name "Giacomo Debidda"]]]
   [:scm
    [:url "https://github.com/jackdbd/fosdem-dl"]
    [:connection "scm:git:https://github.com/jackdbd/fosdem-dl.git"]
    [:developerConnection "scm:git:ssh:git@github.com:jackdbd/pod-jackdbd-jsoup.git"]
    [:tag (str "v" version)]]])

(defn- shared-config [opts]
  (let [vers (:version project)]
    (assoc opts
           :basis (b/create-basis {:project "deps.edn"})
           :class-dir "target/classes"
           :jar-file (format "target/%s-%s.jar" (name lib) vers)
           :lib lib
           :main 'pod.jackdbd.jsoup
           :pom-data (pom-template {:version vers})
           :src-dirs ["src"]
           :target "target"
           :uber-file (format "target/%s-%s-standalone.jar" (name lib) vers)
           :version vers)))

(defn uber "Build the uber-JAR." [opts]
  (let [config (shared-config opts)
        {:keys [basis class-dir main pom-data src-dirs target uber-file version]} config]

    (clean nil)

    (println "\nWriting" (b/pom-path (select-keys config [:lib :class-dir])) "...")
    (b/write-pom {:basis basis
                  :class-dir class-dir
                  :lib lib
                  :pom-data pom-data
                  :src-dirs src-dirs
                  :target target
                  :version version})

    (println "\nCopying src and resources ...")
    (b/copy-dir {:src-dirs ["src" "resources"] :target-dir class-dir})

    (println "\nBuilding" uber-file "...")
    ;; https://clojure.github.io/tools.build/clojure.tools.build.api.html#var-compile-clj
    (b/compile-clj {:basis basis :class-dir class-dir :src-dirs ["src"]})
    ;; https://clojure.github.io/tools.build/clojure.tools.build.api.html#var-uber:version version})
    (b/uber {:basis basis
             :class-dir class-dir
             :main main
             :uber-file uber-file})))
