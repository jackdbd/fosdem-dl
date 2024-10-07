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

(defn unknown-command-cli [command]
  (let [xs [(format "Unknown command: %s" command)
            (format "Available commands: %s" (str/join ", " available-commands))]
        stdout (str/join "\n" xs)]
    {:exit-code 1 :stdout stdout}))

(defn -main
  [& _args]

  ;; (println "=== args ===" _args)
  ;; (println "=== *command-line-args* ===" *command-line-args*)
  ;; (prn "=== *file* ===" *file*)
  ;; (prn "(System/getProperty babashka.file) is" (System/getProperty "babashka.file"))

  (let [command (first *command-line-args*)
        result (case command
                 nil (help)
                 "help" (help)
                 "talks" (talks-cli (rest *command-line-args*))
                 "tracks" (tracks-cli (rest *command-line-args*))
                 (unknown-command-cli command))]
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

(when (System/getProperty "babashka.file")
  (-main *command-line-args*))
