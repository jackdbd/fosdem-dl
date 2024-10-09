(ns user
  (:require
   [babashka.http-client :as http]
   [clojure.java.io :as io]
   [fosdem-dl.download :refer [download-all!]]
   [fosdem-dl.pods :refer [pod-specs]]
   [fosdem-dl.scraping :refer [attachment-urls
                               conference-track-urls
                               jsoup-node-element->href
                               maybe-video-url]]
   [fosdem-dl.talks-cli :refer [talks-cli]]
   [fosdem-dl.tracks-cli :refer [tracks-cli]]
   [babashka.pods :as pods]))

(comment
  ;; This page has no video
  (def url "https://fosdem.org/2023/schedule/event/test_talk1/")
  (def response (http/get url {:throw false}))
  (def html (:body response))
  (maybe-video-url {:html html})

  ;; This page has 1 video and 1 attachment (the slides of the presentation)
  (def url "https://archive.fosdem.org/2020/schedule/event/dqlite/")
  (def response (http/get url {:throw false}))
  (def html (:body response))
  (maybe-video-url {:html html})
  (attachment-urls {:html html})

  (pods/unload-pod (:jsoup @pod-specs))

  (require '[babashka.pods :as pods])
  (def pod-spec (pods/load-pod 'com.github.jackdbd/jsoup "0.4.0"))
  (require '[pod.jackdbd.jsoup :as jsoup])

  (def links (jsoup/select html ".event-attachments>li>a[href]"))

  (def url "https://archive.fosdem.org/2020/schedule/event/seccomp/")
  (def timeout 10000)
  (def response (http/get url {:throw false :timeout timeout}))
  (def html (:body response))

  (jsoup/select html "div")
  (jsoup/select html "video>source")

  (tracks-cli ["-y" 2020])

  (talks-cli ["--help"])
  (talks-cli ["-y" 2020 "-t" "databases"])

  (def directory "./downloads")
  (.mkdir (io/file directory))

  (def avi-url (maybe-video-url {:html html :video-format :avi}))
  (def webm-url (maybe-video-url {:html html :video-format :webm}))

  (download-all! {:url url :directory directory :timeout timeout})

  (def year 2020)
  (def track "databases")
  (def url (str "https://archive.fosdem.org/" year "/schedule/track/" track "/"))
  (def response (http/get url {:as :stream :throw false}))
  (def html (slurp (:body response)))

  (conference-track-urls {:html html})

  (def nodes (jsoup/select html "table:last-child tr>td:nth-child(2)>a[href]"))
  (doseq [node nodes]
    (prn (jsoup-node-element->href node)))

  (pods/unload-pod pod-spec))
