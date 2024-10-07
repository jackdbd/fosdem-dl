(ns fosdem-dl.talks-cli
  (:require
   [babashka.cli :as cli]
   [babashka.http-client :as http]
   [clojure.string :as str]
   [fosdem-dl.defaults :as default]
   [fosdem-dl.download :refer [download-all!]]
   [fosdem-dl.error-cli :refer [error-fn]]
   [fosdem-dl.scraping :refer [conference-track-urls]]
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

   :help
   {:desc "Help"
    :alias :h}

   :timeout
   {:desc "HTTP connection timout in milliseconds"
    :coerce :int
    :default default/timeout}

   :track
   {:desc (format "Conference track (e.g. %s)" (name default/track))
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
                         (let [xs [(format "Either there was no FOSDEM in %s, or that year there wasn't the track you are interested in." (:value m))
                                   (format "[TIP] Try with another combination of year (e.g. %s) and track (e.g. %s)" default/year (name default/track))]]
                           (str/join "\n" xs)))}}})

(defn help
  [{:keys [spec]}]
  (let [stdout (str/trim (format "
FOSDEM Downloader (talks)

Download all talks given at a conference track at FOSDEM a given year.

Options:
%s

Examples:
fosdem-dl talks -y %s --track %s [options]"
                                 (cli/format-opts {:spec spec})
                                 default/year
                                 (name default/track)))]
    {:exit-code 0 :stdout stdout}))

(comment
  (error-fn {:spec talks-spec}))

(defn talks-cli
  "Talks CLI."
  [args]
  (let [spec talks-spec
        opts (cli/parse-opts args {:spec spec :error-fn error-fn})]

    (if (or (empty? args) (:help opts) (:h opts))
      (help {:spec spec})
      (let [year (:year opts)
            track (name (:track opts))
            url (str "https://archive.fosdem.org/" year "/schedule/track/" track "/")
            response (http/get url {:throw false})]
        (if (= 404 (:status response))
          (let [xs [(format "URL Not Found: %s" url)
                    (format "[TIP] Try with a different combination of year and track")]]
            (error-fn {:spec spec :type :org.babashka/cli :cause :not-found :opts opts :msg (str/join "\n" xs)}))
          (let [hrefs (conference-track-urls {:html (:body response)})]
            (info "Download all talks from URL" url)
            (doseq [url hrefs]
              (download-all! {:url url
                              :directory "./downloads"
                              :format (:format opts)
                              :download-attachments? (:attachments opts)}))))
        {:exit-code 0}))))
