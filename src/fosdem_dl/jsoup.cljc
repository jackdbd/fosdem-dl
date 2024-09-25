(ns fosdem-dl.jsoup
  (:require [clojure.string :as str]))

;; pod-jaydeesimon-jsoup is not yet published on the babashka pod registry, so
;; we need to compile it as a GraalVM native-image
;; https://github.com/jaydeesimon/pod-jaydeesimon-jsoup

#?(:bb  (do (require '[babashka.pods :as pods])
            (pods/load-pod 'org.babashka/postgresql "0.1.2")
            ;; (pods/load-pod "./resources/pod-jaydeesimon-jsoup")
            (require #_'[pod.jaydeesimon.jsoup :as jsoup]
             '[pod.babashka.postgresql :as pg]))
   :clj (do (set! *warn-on-reflection* true)
            (import [org.jsoup Jsoup]
                    [org.jsoup.nodes Attribute Document Element])))

;; https://jsoup.org/cookbook/extracting-data/selector-syntax
;; https://jsoup.org/apidocs/org/jsoup/select/Selector.html

#?(:bb (defn select
         [html selector]
         (jsoup/select html selector))

   :clj (do
          (defn elem->m
            "Maps a jsoup Element into a Clojure map."
            [^Element elem]
            {:id (.id elem)
             :class-names (.classNames elem)
             :tag-name (.normalName elem)
             :attrs (->> (.attributes elem)
                         .iterator
                         iterator-seq
                         (map (juxt (memfn ^Attribute getKey) (memfn ^Attribute getValue)))
                         (into {}))
             :own-text (.ownText elem)
             :text (.text elem)
             :whole-text (.wholeText elem)
             :inner-html (.html elem)
             :outer-html (.outerHtml elem)})

          (defn select
            [^String html ^String selector]
            (let [^Document doc (Jsoup/parse html "UTF-8")
                  elements (.select doc selector)]
              (map elem->m elements)))))

(comment
  (def html (str/join "" ["<div>"
                          "<h1>Some Heading</h1>"
                          "<p class=\"foo\">some paragraph</p>"
                          "<p class=\"bar\">some other paragraph</p>"
                          "</div>"]))

  (def selector "p.foo")
  (select html selector)
  (def selector "p.bar")
  (select html selector))
