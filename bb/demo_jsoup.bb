(ns demo-jsoup
  (:require
   [babashka.http-client :as http]
   [babashka.pods :as pods]
   [cheshire.core :as json]
   [clojure.string :as str]
   [fosdem-dl.scraping :refer [attachment-urls maybe-video-url]]))

(def pod-uberjar-name "pod.jackdbd.jsoup")
(def pod-uberjar-version (System/getenv "POD_JACKDBD_JSOUP_VERSION"))
(def uber-file (format "resources/pod/%s-%s-standalone.jar" pod-uberjar-name pod-uberjar-version))

(def pod-binary-name "pod-jackdbd-jsoup")
;; (def pod-binary-version (System/getenv "POD_JACKDBD_JSOUP_VERSION"))
(def exe-file (format "resources/pod/%s" pod-binary-name))

(def html (str/join "" ["<!DOCTYPE html>"
                        "<html lang='en-US'>"
                        "<head>"
                        "  <meta charset='UTF-8'>"
                        "  <title>Hello world</title>"
                        "</head>"
                        "<body>"
                        "  <h1 data-abc=\"def\">Test world</h1>"
                        "  <div class='foo' id='the-foo'><p>This is foo</p></div>"
                        "  <div class='bar'><p data-abc=\"def\">This is bar</p></div>"
                        "  <div class='foo' id='the-other-foo'><p data-abc=\"xyz\">This is another foo</p></div>"
                        "</body>"
                        "</html>"]))

(comment
  ;; Run this code in a Babashka REPL

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

  (pods/unload-pod "pod.jackdbd.jsoup")

  (def pod-spec (pods/load-pod 'com.github.jackdbd/jsoup "0.4.0"))
  (require '[pod.jackdbd.jsoup :as jsoup]) 
  (pods/unload-pod pod-spec)

  ;; Load the pod by evaluating one of the following two lines
  (def pod-spec (pods/load-pod ["java" "-jar" uber-file]))
  (def pod-spec (pods/load-pod exe-file))

  (require '[pod.jackdbd.jsoup :as jsoup])

  (jsoup/select html "div.foo")

  (def parsed (jsoup/select html "div.foo"))
  (def filepath "target/test-html.json")
  (spit filepath (json/generate-string {:html html :parsed parsed}))
  (println (str "wrote " filepath))

  (require '[babashka.http-client :as http])

  (-> (http/get "https://clojure.org")
      :body
      (jsoup/select "div p")
      first
      :text)

  ;; Unload the pod by evaluating one of the following two lines
  (pods/unload-pod (:pod/id pod-spec))
  (pods/unload-pod pod-spec)
  )
