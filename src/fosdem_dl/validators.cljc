(ns fosdem-dl.validators
  (:require
   [fosdem-dl.date-util :refer [current-year]]))

(def valid-video-formats #{:mp4 :webm})

(defn valid-video-format?
  [format]
  (contains? valid-video-formats format))

(comment
  (valid-video-format? :webm)
  (valid-video-format? :mp4)
  (valid-video-format? :avi))

;; There is a FOSDEM 2004 website, but I couldn't find any talk.
(defn valid-talks-year?
  [year]
  (or (= 2003 year)
      (<= 2005 year (current-year))))

(comment
  (valid-talks-year? 2002)
  (valid-talks-year? 2003)
  (valid-talks-year? 2004)
  (valid-talks-year? (current-year)))

;; The page with all conference tracks for a given year is available from 2013.
;; https://archive.fosdem.org/2012/schedule/tracks/
;; https://archive.fosdem.org/2013/schedule/tracks/
(defn valid-tracks-year?
  [year]
  (<= 2013 year (current-year)))
