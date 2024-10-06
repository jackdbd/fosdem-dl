#!/usr/bin/env bb

(ns fosdem-dl.cli
  "CLI to download the talks given at FOSDEM over the years."
  (:gen-class)
  (:require
   [fosdem-dl.talks-cli :refer [talks-cli]]
   [fosdem-dl.tracks-cli :refer [tracks-cli]]))

(def available-subcommands #{"talks" "tracks"})

(defn default-cli [args]
  (let [subcommand (first args)]
    (println (format "[ERROR] Unknown subcommand: %s" subcommand))
    (println "Available subcommands:" (str available-subcommands))
    {:exit-code 1}))

(defn -main
  [args]

  ;; (println "=== args ===" args)
  ;; (println "=== *command-line-args* ===" *command-line-args*)
  ;; (prn "*file* is" *file*)
  ;; (prn "(System/getProperty babashka.file) is" (System/getProperty "babashka.file"))

  (let [subcommand (first args)
        result (case subcommand
                 "talks" (talks-cli (rest args))
                 "tracks" (tracks-cli (rest args))
                 (default-cli args))]
    ;; (System/exit {:exit-code result})
    {:exit-code result}))

(comment
  (-main ["tracks" "-y" 2023])
  (-main ["talks" "-y" 2023 "-t" "test"]))

;; (def schedule-page "https://fosdem.org/2021/schedule/")
;; (def video-page "https://video.fosdem.org/")

(-main *command-line-args*)
