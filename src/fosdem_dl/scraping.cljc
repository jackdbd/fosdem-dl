(ns fosdem-dl.scraping
  (:require
   #?(:bb [pod.jackdbd.jsoup :as jsoup]
      :clj  [babashka.pods :as pods])
   [clojure.core :refer [format]]
   [clojure.string :as str]
   [fosdem-dl.defaults :as default]
   [taoensso.timbre :refer [debug warn]]))

;; When running on Babashka we have already imported the pod, so we have nothing
;; to require/load here. We still need to define a match for the reader
;; conditional though, because otherwise Babashka will match the :clj branch.
;; Also, make sure the version of the pod is the same as the one in the bb.edn.
#?(:bb  nil
   :clj (do (require '[fosdem-dl.pods :refer [pod-specs]])
            (let [pod-spec (pods/load-pod 'com.github.jackdbd/jsoup "0.4.0")]
              (swap! pod-specs assoc :jsoup pod-spec)
              (require '[pod.jackdbd.jsoup :as jsoup]))))

(defn maybe-video-url
  "Extracts the <video> `src` attribute from an HTML string."
  [{:keys [html video-format]
    :or {video-format default/video-format}}]
  (let [selector "video>source"
        video-src (-> html
                      #_{:clj-kondo/ignore [:unresolved-namespace]}
                      (jsoup/select selector)
                      first
                      :attrs
                      (get "src"))]

    (if (not video-src)
      (do
        (warn "Could not find any video in HTML using selector" selector)
        nil)
      (let [[webm-filename room year] (->> (str/split video-src #"/")
                                           reverse
                                           (take 3))
            webm-url (format "https://ftp.fau.de/fosdem/%s/%s/%s" year room webm-filename)]
        (case video-format
          :mp4 (-> (str/split webm-url #"webm") first (str "mp4"))
          :webm webm-url
          nil)))))

(defn jsoup-node-element->href
  [{attrs :attrs}]
  (let [end (get attrs "href")]
    (str "https://fosdem.org" end)))

(defn attachment-urls
  [{:keys [html]}]
  (let [selector ".event-attachments>li>a[href]"
        links #_{:clj-kondo/ignore [:unresolved-namespace]}
        (jsoup/select html selector)]
    (debug "Found" (count links) "links in HTML using selector" selector)
    (map jsoup-node-element->href links)))

(defn conference-track-urls
  "Scrapes a conference track page for the URLs of the talks.
   Example: https://archive.fosdem.org/2020/schedule/track/databases/"
  [{:keys [html]}]
  (let [selector "table:last-child tr>td:nth-child(2)>a[href]"
        links #_{:clj-kondo/ignore [:unresolved-namespace]}
        (jsoup/select html selector)]
    (debug "Found" (count links) "links in HTML using selector" selector)
    (map jsoup-node-element->href links)))

(defn conference-tracks-urls
  "Scrapes a page that lists all conference tracks.
   Example: https://archive.fosdem.org/2020/schedule/tracks/"
  [{:keys [html]}]
  (let [selector "tr>td:first-child>a[href]"
        links #_{:clj-kondo/ignore [:unresolved-namespace]}
        (jsoup/select html selector)]
    (debug "Found" (count links) "links in HTML using selector" selector)
    (map jsoup-node-element->href links)))
