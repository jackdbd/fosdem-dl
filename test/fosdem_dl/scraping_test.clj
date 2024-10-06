(ns fosdem-dl.scraping-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]
   [fosdem-dl.scraping :refer [maybe-video-url]]))

(def html-no-video
  (str/join
   ""
   ["<!DOCTYPE html>"
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

(def html-fosdem-video
  (str/join
   ""
   ["<!DOCTYPE html>"
    "<html lang='en-US'>"
    "<head>"
    "  <meta charset='UTF-8'>"
    "  <title>Hello world</title>"
    "</head>"
    "<body>"
    "  <h1 data-abc=\"def\">Test world</h1>"
    "  <div class='video'>"
    "    <video preload='none' controls='controls' width='75%'>"
    "    <source src='https://video.fosdem.org/2024/h2215/fosdem-2024-3120-synergy-in-open-communities.av1.webm' type='video/webm'>"
    "    <source src='https://video.fosdem.org/2024/h2215/fosdem-2024-3120-synergy-in-open-communities.mp4' type='video/mp4'>"
    "    </video>"
    "  </div>"
    "</body>"
    "</html>"]))

(deftest video-url
  (testing "is null when HTML has no <video>"
    (is (nil? (maybe-video-url {:html html-no-video}))))
  (testing "is not null when HTML has a <video>"
    (let [url (maybe-video-url {:html html-fosdem-video})]
      (is (not (nil? url)))))
  (testing "is hosted on ftp.fau.de"
    (let [url (maybe-video-url {:html html-fosdem-video})
          domain "ftp.fau.de"
          video-format "webm"]
      (is (str/includes? url domain))
      (is (= (format "https://%s/fosdem/2024/h2215/fosdem-2024-3120-synergy-in-open-communities.av1.%s" domain video-format)
             url)))))
