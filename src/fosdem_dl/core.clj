#!/usr/bin/env bb

(ns fosdem-dl.core
  "CLI script to download the talks given at FOSDEM over the years
  https://fosdem.org/"
  (:require [clojure.string :as str]
            [cheshire.core :as json]
            [clojure.tools.cli :refer [parse-opts]]
            [babashka.curl :as curl]
            [babashka.pods :as pods]
            [clojure.java.io :as io])
  (:import [java.lang Thread]))

(pods/load-pod 'org.babashka/etaoin "0.0.1")
(require '[pod.babashka.etaoin :as eta])

(def schedule-page "https://fosdem.org/2021/schedule/")
(def video-page "https://video.fosdem.org/")
;; TODO: tracks as command line argument, or maybe from EDN file?
(def track-page-example "https://archive.fosdem.org/2020/schedule/track/web_performance/")
;; XPath selector for links in track webpages
;; $x ("//table[last()]/tbody/tr/td[position() = 2]/a/@href")

;; (def webm-example "https://video.fosdem.org/2020/H.1309/webperf_qoe_research.webm")
(def webm-example "https://ftp.fau.de/fosdem/2020/H.1309/webperf_qoe_research.webm")
(def mp4-example "https://ftp.fau.de/fosdem/2020/AW1.125/mgmtconfigmore.mp4")
(def zip-example "http://ipv4.download.thinkbroadband.com/5MB.zip")
;; (def zip-example "http://ipv4.download.thinkbroadband.com/200MB.zip")
(def attachments-example '("https://archive.fosdem.org/2020/schedule/event/webperf_qoe_research/attachments/slides/3686/export/events/attachments/webperf_qoe_research/slides/3686/FOSDEM2020_webqoe_drossi.pdf"
                           "https://archive.fosdem.org/2020/schedule/event/webperf_boomerang_optimisation/attachments/slides/3750/export/events/attachments/webperf_boomerang_optimisation/slides/3750/Check_Yourself_Before_You_Wreck_Yourself.pdf"))

;; There is a FOSDEM 2004 website, but I couldn't find any talk.
(defn validate-year
  [year]
  (or (= 2003 year) (<= 2005 year 2021)))

(defn validate-video-format
  [format]
  (or (= "webm" format) (= "mp4" format)))

;; TODO: video format: webm mp4
(def cli-options
  [["-y" "--year YEAR" "Year of the FOSDEM conference"
    :default 2020
    :parse-fn #(Integer/parseInt %)
    :validate [validate-year "Must be 2003 or a number between 2005 and 2021"]]
   ["-f" "--format VIDEO-FORMAT" "Video format; webmp (default) or mp4"
    :default "webm"
    :validate [validate-video-format "Must be either webm or mp4"]]
   ["-t" "--track TRACK" "Conference track"]
   ["-a" "--attachments"]
   ["-h" "--help"]])

(defn shutdown-hook
  []
  (-> (Runtime/getRuntime)
      (.addShutdownHook (Thread. #(println "...")))))

;; TODO: include the talk's links too? Maybe write them in a text/markdown file?
(def usage-help
  (->> ["Usage: fosdem-dl [OPTION]..."
        "Download talks given at the FOSDEM conference over the years."
        ""
        "  -y, --year YEAR      Select given year (default: 2020)"
        "  -t, --track TRACK    Select a conference track (e.g. web_performance)"
        "  -f, --format FORMAT  Select format (default: webm)"
        "  -a, --attachments    Download also the talk's attachments (PDFs, slides, etc)"
        "  -h, --help           Show this help and exit"
        ""]
       (str/join \newline)))

(defn screenshot
  [year track]
  (let [driver (eta/chrome)
        url (str "https://archive.fosdem.org/" year "/schedule/track/" track "/")
        fname (str year "-" track ".png")]
    (eta/go driver url)
    (eta/screenshot driver fname)
    (eta/quit driver)))

(defn- raw-args
  [attachments]
  (->> (map #(conj ["-O"] %) attachments)
       (flatten)))

(defn- get-attachments
  []
  (let [attachments attachments-example
        resp (curl/get nil {:debug true
                            :raw-args (raw-args attachments)
                            :throw false})]
    (when (= 200 (:status resp))
      (println (str "attachments downloaded")))))

(defn- get-video
  [url]
  (let [resp (curl/get url {:debug true
                            ;; :as :stream
                            :raw-args ["-O" "--verbose"]
                            :throw false})]
    (when (= 200 (:status resp))
      (println (str "video at " url " downloaded")))
    (comment
      (json/parse-string (:body resp))
      (println "ERR " (:err resp))
      (println "EXIT " (:exit resp))
      (println "PROCESS " (:process resp))
      (println "COMMAND " (:command resp))
      (println "OPTIONS " (:options resp))
      (println "HEADERS " (:headers resp)))))

;; TODO: find the URLs for the attachments starting from the talk's main page
(defn download-webm
  [url download-attachments]
  (when download-attachments
    (get-attachments))
  (get-video url))

(defn download-mp4
  [url download-attachments]
  (when download-attachments
    (get-attachments))
  (get-video url))

(defn -main
  "You can run this program with [babashka](https://github.com/babashka/babashka):
     - chmod +x src/fosdem_dl/core.clj
     - bb src/fosdem_dl/core.clj"
  [& args]
  (when (= 0 (count args))
    (println usage-help)
    (System/exit 1))
  (shutdown-hook)
  (let [options (:options (parse-opts *command-line-args* cli-options))
        coll-contains? (partial contains? options)
        download-attachments (:attachments options)]
    (when (coll-contains? :track)
      (let [track (:track options)
            year (:year options)]
        (screenshot year track)))
    (if (:help options)
      (println usage-help)
      (if (= "webm" (:format options))
        (download-webm webm-example download-attachments)
        (download-mp4 mp4-example download-attachments)))))

;; check if the current file was the file invoked from the command line
;; https://book.babashka.org/#main_file
(when (= *file* (System/getProperty "babashka.file"))
  (-main))
;; (-main *command-line-args*)
;;
;; TODO: how to show curl's progress bar?
