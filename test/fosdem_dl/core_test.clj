(ns fosdem-dl.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [fosdem-dl.core :refer [validate-video-format validate-year]]))

(deftest year-range
  (testing "year cannot be 2004"
    (is (false? (validate-year 2004))))
  (testing "year cannot be smaller than 2003"
    (is (true? (validate-year 2003)))
    (is (false? (validate-year 2002))))
  (testing "year cannot be bigger than 2021"
    (is (true? (validate-year 2021)))
    (is (false? (validate-year 2022)))))

(deftest video-formats
  (testing "format can be mp4"
    (is (true? (validate-video-format "mp4"))))
  (testing "format can be webm"
    (is (true? (validate-video-format "webm"))))
  (testing "format cannot be avi"
    (is (false? (validate-video-format "avi")))))
