
(ns fosdem-dl.date-util
  (:import
   [java.time LocalDate]))

(defn current-year []
  (.getYear (LocalDate/now)))

(comment
  (current-year))
