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
  [& args]

  ;; (println "=== args ===" args)
  ;; (println "=== *command-line-args* ===" *command-line-args*)
  ;; (println "=== *file* ===" *file*)
  ;; (println "(System/getProperty babashka.file) is" (System/getProperty "babashka.file"))

  (let [command (first args)
        result (case command
                 nil (help)
                 "help" (help)
                 "talks" (talks-cli (rest args))
                 "tracks" (tracks-cli (rest args))
                 (unknown-command-cli command))]
    (when-let [stdout (:stdout result)]
      (println stdout))
    (when-let [stderr (:sterr result)]
      (println "ERRORS")
      (println stderr))
    (System/exit (:exit-code result))))

;; TODO: do I need to call pods/unload when exiting? Otherwise it seems the CLI
;; doesn't quit unless I Ctrl+C it.

(comment
  (-main ["tracks" "-y" 2023])
  (-main ["talks" "-y" 2023 "-t" "test"]))

;; (def schedule-page "https://fosdem.org/2021/schedule/")
;; (def video-page "https://video.fosdem.org/")

;; This doesn't seem to pass CLI args to an uberjar
;; If we are running in Babashka or as an uberjar...
;; (when (or (System/getProperty "babashka.file")
;;           (.endsWith (System/getProperty "java.class.path") ".jar"))
;;   (-main *command-line-args*))

(when (System/getProperty "babashka.file")
  (-main *command-line-args*))
