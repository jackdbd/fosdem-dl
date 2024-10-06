(ns fosdem-dl.validators-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [fosdem-dl.date-util :refer [current-year]]
   [fosdem-dl.validators :refer [valid-video-format? valid-talks-year? valid-tracks-year?]]))

(deftest talks-year
  (testing "cannot be 2004"
    (is (false? (valid-talks-year? 2004))))
  (testing "cannot be smaller than 2003"
    (is (false? (valid-talks-year? 2002)))
    (is (true? (valid-talks-year? 2003))))
  (testing "cannot be bigger than current year"
    (is (true? (valid-talks-year? (current-year))))
    (is (false? (valid-talks-year? (inc (current-year)))))))

(deftest tracks-year
  (testing "cannot be 2004"
    (is (false? (valid-tracks-year? 2004))))
  (testing "cannot be smaller than 2013"
    (is (false? (valid-tracks-year? 2012)))
    (is (true? (valid-tracks-year? 2013))))
  (testing "cannot be bigger than current year"
    (is (true? (valid-tracks-year? (current-year))))
    (is (false? (valid-tracks-year? (inc (current-year)))))))

(deftest video-formats
  (testing "can be mp4"
    (is (true? (valid-video-format? :mp4))))
  (testing "can be webm"
    (is (true? (valid-video-format? :webm))))
  (testing "cannot be avi"
    (is (false? (valid-video-format? :avi)))))
