(ns user
  (:require [clojure.java.io :as io]
            [cuic.core :as c]
            [cuic.chrome :as chrome]
            [fosdem-dl.jsoup :as jsoup]
            [fosdem-dl.util :as util]))

(prn "hello from user.clj")

;; (require '[babashka.deps :as deps])
;; (deps/add-deps '{:deps {etaoin/etaoin {:mvn/version "1.0.38"}}})

;; (eta/go driver "https://en.wikipedia.org/")
;; wait for the search input to load
;; (eta/wait-visible driver [{:id :simpleSearch} {:tag :input :name :search}])

(comment
  (def launch-options {:disable-gpu false
                       :headless false
                       :no-first-run true
                       :remote-debugging-port 9222})

  ; be careful on how you specify the path to your Chrome binary
; https://github.com/milankinen/cuic/issues/18
  (def browser
    (chrome/launch launch-options (.toPath (io/file "/usr/bin/google-chrome-stable"))))

  (c/set-browser! browser)

  ;; (def url "https://archive.fosdem.org/2003/index/schedule.html")
  ;; (def url "https://archive.fosdem.org/2003/index/tracks.html")
  ;; (def url "https://archive.fosdem.org/2004/2004/index/schedule.html")
  ;; (def url "https://archive.fosdem.org/2005/2005/index/tracks.html")
  ;; (def url "https://archive.fosdem.org/2020/schedule/track/databases/")

  (def year 2020)
  (def track "web_performance")
  (def url (str "https://archive.fosdem.org/" year "/schedule/track/" track "/"))

  (c/goto url)

  (def url "https://video.fosdem.org/2020/H.1309/webperf_boomerang_optimisation.webm")
  (util/get-video url)

;; (require '[clojure.java.io :as io]
  ;;          '[cuic.core :as c]
  ;;          '[cuic.chrome :as chrome])
  ;; (require '[etaoin.api :as e]
  ;;          '[etaoin.keys :as k]

;; https://github.com/clj-commons/etaoin/blob/master/doc/01-user-guide.adoc#installing-the-browser-webdrivers
  ;; (def driver (e/firefox {}))
  ;; (def driver (eta/chrome {}))
  ;; (eta/driver? driver :chrome)
  )

(comment
  (def html
    "<div><p class=\"foo\">a foo paragraph</p><p class=\"bar\">a bar paragraph</p></div>")

  (jsoup/select html "p.foo")
  (jsoup/select html "p.bar")
  (jsoup/select html "p.baz"))
