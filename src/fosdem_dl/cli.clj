#!/usr/bin/env bb

(ns fosdem-dl.cli
  "CLI to download the talks given at FOSDEM over the years."
  (:gen-class)
  (:require
   [clojure.string :as str]
   [fosdem-dl.talks-cli :refer [talks-cli]]
   [fosdem-dl.tracks-cli :refer [tracks-cli]]))

(def available-commands #{"talks" "tracks"})

(defn help
  []
  (let [stdout (str/trim (format "
FOSDEM Downloader

Usage: fosdem-dl <command> [options]
Available commands: %s" (str/join ", " available-commands)))]
    {:exit-code 0 :stdout stdout}))

(defn unknown-command-cli [args]
  (let [command (first args)
        xs [(format "Unknown command: %s" command)
            (format "Available commands: %s" (str/join ", " available-commands))]
        stdout (str/join "\n" xs)]
    {:exit-code 1 :stdout stdout}))

(defn -main
  [args]

  ;; (println "=== args ===" args)
  ;; (println "=== *command-line-args* ===" *command-line-args*)
  ;; (prn "*file* is" *file*)
  ;; (prn "(System/getProperty babashka.file) is" (System/getProperty "babashka.file"))

  (let [command (first args)
        result (case command
                 nil (help)
                 "talks" (talks-cli (rest args))
                 "tracks" (tracks-cli (rest args))
                 (unknown-command-cli args))]
    (when-let [stdout (:stdout result)]
      (println stdout))
    (when-let [stderr (:sterr result)]
      (println "ERRORS")
      (println stderr))
    (:exit-code result)))

(comment
  (-main ["tracks" "-y" 2023])
  (-main ["talks" "-y" 2023 "-t" "test"]))

;; (def schedule-page "https://fosdem.org/2021/schedule/")
;; (def video-page "https://video.fosdem.org/")

(-main *command-line-args*)
