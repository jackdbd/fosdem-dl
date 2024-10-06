(ns fosdem-dl.talks-cli
  (:require
   [babashka.cli :as cli]
   [babashka.http-client :as http]
   [clojure.string :as str]
   [fosdem-dl.defaults :as default]
   [fosdem-dl.download :refer [download-all!]]
   [fosdem-dl.scraping :refer [conference-track-urls]]
   [fosdem-dl.shared-cli :refer [error-fn print-help]]
   [fosdem-dl.validators :refer [valid-video-formats valid-video-format? valid-talks-year?]]
   [taoensso.timbre :refer [info]]))

(def talks-spec
  {:attachments
   {:desc "Whether to download each talk's attachments (PDFs, slides, etc)"
    :alias :a
    :coerce :bool
    :default default/download-attachments?}

   :format
   {:desc (format "Video format %s" (str valid-video-formats))
    :alias :f
    :coerce :keyword
    :default default/video-format
    :validate valid-video-format?}

   :timeout
   {:desc "HTTP connection timout in milliseconds"
    :coerce :int
    :default default/timeout}

   :track
   {:desc "Conference track (e.g. databases, web_performance)"
    :alias :t
    :coerce :keyword
    :require true}

   :year
   {:desc "Year of FOSDEM"
    :alias :y
    :coerce :int
    :require true
    :validate {:pred valid-talks-year?
               :ex-msg (fn [m]
                         (let [xs [(format "[INVALID] Either there was no FOSDEM in %s, or that year there wasn't the track you are interested in." (:value m))
                                   (format "[TIP] Try with another combination of year (e.g. %s) and track (e.g. %s)" default/year default/track)]]
                           (str/join "\n" xs)))}}})

(comment
  (error-fn {:spec talks-spec}))

(defn talks-cli
  "Talks CLI."
  [args]
  (let [spec talks-spec
        opts (cli/parse-opts args {:spec spec :error-fn error-fn})]

    (if (or (:help opts) (:h opts))
      (println (print-help {:spec spec}))
      (let [year (:year opts)
            track (name (:track opts))
            url (str "https://archive.fosdem.org/" year "/schedule/track/" track "/")
            response (http/get url {:throw false})]
        (if (= 404 (:status response))
          (println "URL not found:" url)
          (let [hrefs (conference-track-urls {:html (:body response)})]
            (info "Download all talks from URL" url)
            (doseq [url hrefs]
              (download-all! {:url url
                              :directory "./downloads"
                              :format (:format opts)
                              :download-attachments? (:attachments opts)}))))))
    {:exit-code 0}))
