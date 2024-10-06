(ns tasks
  (:require
   [babashka.classpath :refer [get-classpath split-classpath]]
   [babashka.pods :as pods]
   [cheshire.core :as json]
   [clojure.string :as str]))

(def pod-id "pod.jackdbd.jsoup")
(def pod-name "pod-jackdbd-jsoup")
(def pod-version (System/getenv "POD_JACKDBD_JSOUP_VERSION"))
(def uber-file (format "resources/pod/%s-%s-standalone.jar" pod-id pod-version))
(def exe-file (format "resources/pod/%s" pod-name))

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

(defn print-classpath []
  (println "=== CLASSPATH BEGIN ===")
  (doseq [path (set (split-classpath (get-classpath)))]
    (println path))
  (println "=== CLASSPATH END ==="))

(comment
  (pods/load-pod ["java" "-jar" uber-file])
  (pods/load-pod exe-file)

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
  )
