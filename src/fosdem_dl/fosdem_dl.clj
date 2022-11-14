#!/usr/bin/env bb

(ns fosdem-dl.fosdem-dl
  "CLI script to download the talks given at FOSDEM over the years"
  (:require [babashka.cli :as cli]
            [babashka.curl :as curl]
            [clojure.string :as str]
            [fosdem-dl.jsoup :as jsoup]
            [fosdem-dl.util :refer [m->href platform-info valid-year? download-all!]])
  ;; (:import [java.lang Thread]) 
  (:gen-class))

(def spec {:from {:ref "<format>"
                  :desc "The input format. <format> can be edn, json or transit"
                  :alias :i}

           :year {:desc "The year"
                  :alias :y
                  :coerce :int
                  :validate valid-year?
                  :default 2020}

           ;; -a, --attachments    Download also the talk's attachments (PDFs, slides, etc)

           :track {:desc "The Conference track"
                   :alias :t
                  ;;  :require true
                   :coerce :keyword
                   :default :web_performance}

           :format {:desc "The video format"
                    :alias :f
                    :coerce :keyword
                    :default :webm}})

(defn url->hrefs!
  "Fetch `url` and extract the href property from all <a> tags."
  [url]
  (let [resp (curl/get url {:throw false})
        status (:status resp)]
    (if (= 200 status)
      (let [links (-> resp
                      :body
                      (jsoup/select "table:last-child tr>td:nth-child(2)>a[href]"))
            hrefs (map m->href links)]
        hrefs)
      (println (str "Cannot fetch " url " (status: " status ")")))))

(defn print-help
  []
  (println (str/trim "
FOSDEM Downloader

Usage: fosdem-dl <subcommand>
"))
  (println (cli/format-opts {:spec spec :order [:year :track :format :from]})))

(defn custom-error-fn
  [{:keys [spec type cause msg option] :as data}]
  (println "spec" spec)
  (println "type" type)
  (println "msg" msg)
  (println "option" option)
  (println "cause" cause)
  (println "data" data))

(defn -main
  [& args]
  (platform-info)

  ;; (println "=== *command-line-args* ===" *command-line-args*)
  ;; (println "=== args ===" args)
  ;; (prn "*file*" *file*)
  ;; (prn "===" (System/getProperty "babashka.file"))
  (when (= *file* (System/getProperty "babashka.file"))
    (println "=== apply command line args ===" *command-line-args*))

  (let [m (cli/parse-opts args {:spec spec})]
    (prn "m" m)
    (if (or (nil? args) (:help m))
      (do
        (print-help)
        (System/exit 0))
      (let [year (:year m)
            track (name (:track m))
            ;; video-format (:format m)
            video-format "webm"
            dest "./downloads/"
            should-download-attachments true
            url (str "https://archive.fosdem.org/" year "/schedule/track/" track "/")
            hrefs (url->hrefs! url)]
        (println "url" url)
        (prn hrefs)
        (doseq [url hrefs]
          (download-all! {:url url
                          :dest dest
                          :format video-format
                          :should-download-attachments should-download-attachments}))
        (System/exit 0)))))

;; from 2013 to 2021 the structure of the webpage of any conference track stays the same

;; (def schedule-page "https://fosdem.org/2021/schedule/")
;; (def video-page "https://video.fosdem.org/")

;; (defn shutdown-hook
;;   []
;;   (-> (Runtime/getRuntime)
;;       (.addShutdownHook (Thread. #(println "...")))))

;; (defn -main
;;   "You can run this program with [babashka](https://github.com/babashka/babashka):
;;      - chmod +x src/fosdem_dl/core.clj
;;      - bb src/fosdem_dl/core.clj"
;;   [& args]
;;   (when (= 0 (count args))
;;     (println usage-help)
;;     (System/exit 1))
;;   (shutdown-hook)
;;   (let [options (:options (parse-opts *command-line-args* cli-options))
;;         coll-contains? (partial contains? options)]
;;     (when (coll-contains? :help)
;;       (println usage-help)
;;       (System/exit 0))
;;     (let [should-download-attachments (coll-contains? :attachments)
;;           year (:year options)
;;           track (:track options)
;;           url (str "https://archive.fosdem.org/" year "/schedule/track/" track "/")
;;         ;; TODO: make sure that this directory exists, otherwise curl fails (silently because we use :throw false)
;;           dest (str "./downloads/")
;;           hrefs (url->hrefs! url)]
;;       (doseq [url hrefs]
;;         (download-all! url dest (:format options) should-download-attachments)))))

;; ;; check if the current file was the file invoked from the command line
;; ;; https://book.babashka.org/#main_file
;; (when (= *file* (System/getProperty "babashka.file"))
;;   (-main))
;; ;; (-main *command-line-args*)

;; (comment
;;   (-main {:year 2020
;;           :track "web_performance"
;;           :attachments true
;;           :format "webm"}))

