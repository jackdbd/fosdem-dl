(ns fosdem-dl.util
  (:require [babashka.curl :as curl]
            [clojure.string :as str]
            [fosdem-dl.jsoup :as jsoup]
            [progrock.core :as pr]))

;; [babashka.classpath :as cp]

(defn platform-info []
  #?(:bb (println "Running on Babashka")
     :clj (println "Running on Clojure")
     :default (println "Running on some other platform")))

;; There is a FOSDEM 2004 website, but I couldn't find any talk.
(defn valid-year?
  [year]
  (or (= 2003 year)
      (<= 2005 year 2021)))

(defn validate-video-format
  [format]
  (or (= "webm" format)
      (= "mp4" format)))

(defn m->href
  [{attrs :attrs}]
  (let [end (get attrs "href")]
    (str "https://fosdem.org" end)))

;; (defn print-classpath []
;;   (println "=== CLASSPATH BEGIN ===")
;;   (prn (cp/split-classpath (cp/get-classpath)))
;;   (println "=== CLASSPATH END ==="))

(defn get-video
  [url]
  (let [resp (curl/get url {:debug true
                            ;; :as :stream
                            :raw-args ["-O" "--verbose"]
                            :throw false})]
    (when (= 200 (:status resp))
      (println (str "video at " url " downloaded")))))

(defn download-video!
  "Extracts the <video> `src` attr from `body` and downloads the video file, in
   the specified `format`, to location `dest`."
  [body dest video-format]
  (let [bar (pr/progress-bar 100)
        video-src (-> body
                      (jsoup/select "video>source")
                      first
                      :attrs
                      (get "src"))
        [webm-filename room year] (->> (str/split video-src #"/")
                                       reverse
                                       (take 3))
        webm-url (format "https://ftp.fau.de/fosdem/%s/%s/%s/" year room webm-filename)]
    
    ;; TODO: render a meaningful progress in the progress bar
    (pr/print (pr/tick bar 25))
    
    (case video-format
      "webm" (let [filepath (str dest webm-filename)
                   _resp (curl/get webm-url {:raw-args ["-o" filepath]
                                             :throw false})])
      "mp4" (let [mp4-filename (-> (str/split webm-filename #"webm") first (str "mp4"))
                  filepath (str dest mp4-filename)
                  mp4-url (-> (str/split webm-url #"webm") first (str "mp4"))
                  _resp (curl/get mp4-url {:raw-args ["-o" filepath]
                                           :throw false})])
      :else (println "format" video-format "not supported."))))

(defn- download-attachment!
  "Downloads the file found at `url`, to location `dest`."
  [url dest]
  (let [filename (-> (str/split url #"/") last)
        filepath (str dest filename)
        resp (curl/get url {:raw-args ["-o" filepath]
                            :throw false})]
    (when-not (= 200 (:status resp))
      (println url "Could not download" url))))

;; (def download-attachments! (partial on-attachments download-attachment!))

(defn download-all!
  "Downloads video and attachments found at `url`, to location `dest`."
  [{:keys [url dest format should-download-attachments] :as m}]
  (println "download-all!" m)
  (let [body (-> (curl/get url {:throw false}) :body)]
    (when should-download-attachments
      (let [links (jsoup/select body ".event-attachments>li>a[href]")
            hrefs (map m->href links)]
        (doseq [url hrefs]
          (download-attachment! url dest))))
    (download-video! body dest format)))

(comment
  (platform-info)

  (def url "https://archive.fosdem.org/2020/schedule/track/databases/")
  (m->href {:attrs {"href" "https://example.com/foo"}})

  (def resp (curl/get url {:debug true
                           :raw-args ["-O" "--verbose"]
                           :throw false}))

  (def url "https://archive.fosdem.org/2020/schedule/event/seccomp/")
  (def body (-> (curl/get url {:throw false}) :body))
  (def dest (str/join "" ["./downloads"]))
  (download-video! body dest "webm")

  (download-attachment! url dest)
  
  ;; (format "https://ftp.fau.de/fosdem/%s/%s/%s/" 2020 "ciccio" "foo.webm")

  ;; (json/parse-string (:body resp))
  ;;   (println "ERR " (:err resp))
  ;;   (println "EXIT " (:exit resp))
  ;;   (println "PROCESS " (:process resp))
  ;;   (println "COMMAND " (:command resp))
  ;;   (println "OPTIONS " (:options resp))
  ;;   (println "HEADERS " (:headers resp))) 
  )