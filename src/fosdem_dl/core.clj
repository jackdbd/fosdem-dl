#!/usr/bin/env bb

(ns fosdem-dl.core
  "CLI script to download the talks given at FOSDEM over the years
  https://fosdem.org/"
  (:require [clojure.string :as str]
            [clojure.tools.cli :refer [parse-opts]]
            [babashka.curl :as curl]
            [babashka.pods :as pods])
  (:import [java.lang Thread]))

;; pod-jaydeesimon-jsoup is not yet published on the babashka pod registry, so
;; we need to compile it as a GraalVM native-image
;; https://github.com/jaydeesimon/pod-jaydeesimon-jsoup
(pods/load-pod "./pod-jaydeesimon-jsoup")
(require '[pod.jaydeesimon.jsoup :as jsoup])

;; from 2013 to 2021 the structure of the webpage of any conference track stays the same

(def schedule-page "https://fosdem.org/2021/schedule/")
(def video-page "https://video.fosdem.org/")

;; There is a FOSDEM 2004 website, but I couldn't find any talk.
(defn validate-year
  [year]
  (or (= 2003 year) (<= 2005 year 2021)))

(defn validate-video-format
  [format]
  (or (= "webm" format) (= "mp4" format)))

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

(def usage-help
  (->> ["Usage: fosdem-dl [OPTION]..."
        "Download the talks given at FOSDEM over the years."
        ""
        "  -y, --year YEAR      Select given year (default: 2020)"
        "  -t, --track TRACK    Select a conference track (e.g. web_performance)"
        "  -f, --format FORMAT  Select format (default: webm)"
        "  -a, --attachments    Download also the talk's attachments (PDFs, slides, etc)"
        "  -h, --help           Show this help and exit"
        ""]
       (str/join \newline)))

;; (defn- get-video
;;   [url]
;;   (let [resp (curl/get url {:debug true
;;                             ;; :as :stream
;;                             :raw-args ["-O" "--verbose"]
;;                             :throw false})]
;;     (when (= 200 (:status resp))
;;       (println (str "video at " url " downloaded")))
;;     (comment
;;       (json/parse-string (:body resp))
;;       (println "ERR " (:err resp))
;;       (println "EXIT " (:exit resp))
;;       (println "PROCESS " (:process resp))
;;       (println "COMMAND " (:command resp))
;;       (println "OPTIONS " (:options resp))
;;       (println "HEADERS " (:headers resp)))))

(defn- m->href
  [{attrs :attrs}]
  (let [end (get attrs "href")]
    (str "https://fosdem.org" end)))

(defn- url->hrefs!
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

(defn- download-video!
  "Extract the <video> `src` attr from `body` and download the video file (in
  the specified `format`) to location `dest`."
  [body dest format]
  (let [video-src (-> body
                      (jsoup/select "video>source")
                      first
                      :attrs
                      (get "src"))
        [webm-filename room year] (->> (str/split video-src #"/")
                                       reverse
                                       (take 3))
        webm-url (str "https://ftp.fau.de/fosdem/" year "/" room "/" webm-filename)]
    (case format
      "webm" (let [filepath (str dest webm-filename)
                   _resp (curl/get webm-url {:raw-args ["-o" filepath]
                                             :throw false})])
      "mp4" (let [mp4-filename (-> (str/split webm-filename #"webm") first (str "mp4"))
                  filepath (str dest mp4-filename)
                  mp4-url (-> (str/split webm-url #"webm") first (str "mp4"))
                  _resp (curl/get mp4-url {:raw-args ["-o" filepath]
                                           :throw false})])
      :else (println "format" format "not supported."))))

(defn- download-attachment!
  "Download the file found at `url` to location `dest`."
  [url dest]
  (let [
        filename (-> (str/split url #"/") last)
        filepath (str dest filename)
        resp (curl/get url {:raw-args ["-o" filepath]
                            :throw false})]
    (when-not (= 200 (:status resp))
      (println url "Could not download" url))))

;; (def download-attachments! (partial on-attachments download-attachment!))

(defn- download-all!
  "Download video and attachments found on the `url` page."
  [url dest format should-download-attachments]
  (let [body (-> (curl/get url {:throw false}) :body)]
    (when should-download-attachments
      (let [links (jsoup/select body ".event-attachments>li>a[href]")
            hrefs (map m->href links)]
        (doseq [url hrefs]
          (download-attachment! url dest))))
    (download-video! body dest format)))

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
        coll-contains? (partial contains? options)]
    (when (coll-contains? :help)
      (println usage-help)
      (System/exit 0))
    (let [should-download-attachments (coll-contains? :attachments)
          year (:year options)
          track (:track options)
          url (str "https://archive.fosdem.org/" year "/schedule/track/" track "/")
        ;; TODO: make sure that this directory exists, otherwise curl fails (silently because we use :throw false)
          dest (str "./downloads/")
          hrefs (url->hrefs! url)]
      (doseq [url hrefs]
        (download-all! url dest (:format options) should-download-attachments)))))

;; check if the current file was the file invoked from the command line
;; https://book.babashka.org/#main_file
(when (= *file* (System/getProperty "babashka.file"))
  (-main))
;; (-main *command-line-args*)

(comment
  (-main {:year 2020
          :track "web_performance"
          :attachments true
          :format "webm"}))
