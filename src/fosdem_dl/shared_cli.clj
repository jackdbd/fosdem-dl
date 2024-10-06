(ns fosdem-dl.shared-cli
  (:require
   [babashka.cli :as cli]
   [clojure.pprint :refer [pprint]]
   [clojure.string :as str]))

(defn print-help
  [{:keys [spec]}]
  (println (str/trim "
FOSDEM Downloader

CLI to download the talks given at FOSDEM over the years.

Usage: fosdem-dl <subcommands> [options]
"))
  (println (cli/format-opts {:spec spec})))

(defn error-fn
  [{:keys [spec type cause msg option opts] :as _data}]
  (when (and (= :org.babashka/cli type) (not (or (:help opts) (:h opts))))
    (case cause
      :require (do
                 (println (format "Missing required argument: %s\n" option))
                 (System/exit 1))
      :validate (do (println (format "%s\n" msg))
                    (System/exit 1))
      (do (println (str "Unsupported error cause: " cause))
          (println "\nCLI spec:")
          (pprint spec)
          (System/exit 1)))))
