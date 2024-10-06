(ns fosdem-dl.download
  (:require
   [babashka.http-client :as http]
   [clojure.core :refer [format slurp]]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [fosdem-dl.defaults :as default]
   [fosdem-dl.scraping :refer [maybe-video-url attachment-urls]]
  ;;  [progrock.core :as pr]
   [taoensso.timbre :refer [debug trace] :as timbre])
  (:import
   [java.lang Exception]))

;; TODO: render a meaningful progress in the progress bar
;; bar (pr/progress-bar 100)
;; (pr/print (pr/tick bar 25))

(defn- download!
  "Downloads an asset hosted at URL `url`, to directory `directory`."
  [{:keys [directory url timeout]
    :or {timeout default/timeout}}]
  (let [directory (io/file directory)
        filename (-> (str/split url #"/") last)
        file (io/file directory filename)
        filepath (str file)
        result-dir (try
                     (debug "Trying to create/access directory" directory)
                     (.mkdir (io/file directory))
                     {:error false}
                     (catch Exception e
                       (let [msg (.getMessage ^Throwable e)]
                         (trace (ex-info msg {:causes #{:fs}
                                              :directory directory}))
                         {:error msg})))]

    (if-let [msg (:error result-dir)]
      {:error msg :ok false}
      (try
        (debug "Trying to download" url "to" directory)
        (let [response (http/get url {:as :stream :throw false :timeout timeout})
              status (:status response)]
          (if (not= 200 status)
            {:error (format "Could not download %s (HTTP %s)" url status) :ok false}
            (do
              (debug "Downloaded" url)
              (try
                (debug "Trying to create file" filepath)
                (io/copy (:body response) file)
                {:message (format "Wrote %s" filepath) :ok true}
                (catch Exception e
                  (let [msg (.getMessage ^Throwable e)]
                    (trace (ex-info msg {:causes #{:fs}
                                         :filepath filepath}))
                    {:error msg}))))))
        (catch Exception e
          (let [msg (.getMessage ^Throwable e)]
            (trace (ex-info msg {:causes #{:http}
                                 :url url}))
            {:error msg}))))))

(comment
  (def directory "./downloads")
  (.mkdir (io/file directory))

  (def url "http://joeconway.com/presentations/seccomp-FOSDEM2020.pdf")
  (def timeout 12345)

  (def response (http/get url {:as :stream :throw false :timeout timeout}))
  (def response-status (:status response))
  (def response-body (:body response))

  (timbre/set-level! :trace)
  (timbre/set-level! :debug)
  (download! {:url url :directory directory :timeout timeout})

  (def url "https://archive.fosdem.org/2020/schedule/event/seccomp/")
  (def timeout default/timeout)
  (def response (http/get url {:as :stream :throw false :timeout timeout}))
  (def html (slurp (:body response)))

  (def avi-url (maybe-video-url {:html html :video-format :avi}))
  (def mp4-url (maybe-video-url {:html html :video-format :mp4}))
  (def webm-url (maybe-video-url {:html html :video-format :webm}))

  (download! {:url webm-url :directory directory :timeout timeout})
  (download! {:url mp4-url :directory directory :timeout timeout}))

(defn download-all!
  "Downloads all assets (video and attachments) found at URL `url`, to directory
   `directory`."
  [{:keys [directory download-attachments? timeout url video-format]
    :or {download-attachments? true
         timeout default/timeout
         video-format default/video-format}
    :as _opts}]
  (if download-attachments?
    (debug "Scrape" url "and download attachments and video as" video-format)
    (debug "Scrape" url "and download video as" video-format))
  (let [response (http/get url {:throw false})
        html (:body response)]
    (when download-attachments?
      (let [hrefs (attachment-urls {:html html})]
        (doseq [url hrefs]
          (download! {:url url :directory directory :timeout timeout}))))
    (let [url (maybe-video-url {:html html :video-format video-format})]
      (when url
        (download! {:url url :directory directory :timeout timeout})))))

(comment
  (def timeout default/timeout)
  (def url "https://archive.fosdem.org/2020/schedule/track/databases/")
  (def response (http/get url {:throw false :timeout timeout}))
  (def html (:body response)))
