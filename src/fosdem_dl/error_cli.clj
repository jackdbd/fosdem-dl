(ns fosdem-dl.error-cli
  (:require
   [babashka.cli :as cli]
   [clojure.pprint :refer [pprint]]))

;; TODO: can I avoid calling (System/exit 1) and return {:exit-code 1 :stderr "Some error message"} instead?
;; https://github.com/babashka/cli?tab=readme-ov-file#error-handling

(defn error-fn
  [{:keys [spec type cause msg option opts]
    :as _data}]
  (when (and (= :org.babashka/cli type) (not (or (:help opts) (:h opts))))
    (let [result (case cause
                   :require (do (println
                                 (format
                                  "Missing required argument:\n%s"
                                  (cli/format-opts {:spec (select-keys spec [option])})))
                                (System/exit 1))
                   :validate (do (println (format "Invalid:\n%s" msg))
                                 (System/exit 1))
                   :not-found (do (println msg)
                                  (System/exit 1))
                   (do (println (str "Unsupported error cause: " cause))
                       (println "\nCLI spec:")
                       (pprint spec)
                       (pprint _data)
                       (System/exit 1)))]
      result)))
