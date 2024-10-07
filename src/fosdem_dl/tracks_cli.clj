(ns fosdem-dl.tracks-cli
  (:require
   [babashka.cli :as cli]
   [babashka.http-client :as http]
   [clojure.string :as str]
   [fosdem-dl.defaults :as default]
   [fosdem-dl.error-cli :refer [error-fn]]
   [fosdem-dl.scraping :refer [conference-tracks-urls]]
   [fosdem-dl.validators :refer [valid-tracks-year?]]))

(def tracks-spec
  {:help
   {:desc "Help"
    :alias :h}
   :year
   {:desc "Year of FOSDEM"
    :alias :y
    :coerce :int
    :require true
    :validate {:pred valid-tracks-year?
               :ex-msg (fn [m]
                         (let [xs [(format "Either there was no FOSDEM in %s, or the page https://archive.fosdem.org/%s/schedule/tracks/ does not exist." (:value m) (:value m))
                                   (format "[TIP] Try with another year (e.g. %s)" default/year)]]
                           (str/join "\n" xs)))}}})

(defn help
  [{:keys [spec]}]
  (let [stdout (str/trim (format "
FOSDEM Downloader (tracks)

List the tracks at FOSDEM a given year.

Options:
%s

Examples:
fosdem-dl tracks -y %s" (cli/format-opts {:spec spec}) default/year))]
    {:exit-code 0 :stdout stdout}))

(defn tracks-cli
  "Tracks CLI."
  [args]
  (let [spec tracks-spec
        opts (cli/parse-opts args {:spec spec :error-fn error-fn})]
    (if (or (empty? args) (:help opts) (:h opts))
      (help {:spec spec})
      (let [year (:year opts)
            url (format "https://archive.fosdem.org/%s/schedule/tracks/" year)
            response (http/get url {:throw false})
            hrefs (conference-tracks-urls {:html (:body response)})]
        (println (format "FOSDEM %s had a total of %s conference tracks" year (count hrefs)))
        (doseq [url hrefs]
          (println url))
        {:exit-code 0}))))

(comment
  (error-fn {:spec tracks-spec}))
