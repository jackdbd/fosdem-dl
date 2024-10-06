#!/usr/bin/env bb

(ns test-runner
  "Babashka test runner.
   See: https://book.babashka.org/#_running_tests"
  (:require
   [clojure.test :as t]
   [babashka.classpath :as cp]
   [taoensso.timbre :as timbre]))

(cp/add-classpath "src:test")

(timbre/set-level! :debug)
(timbre/debug "Running tests")

(require 'fosdem-dl.scraping-test
         'fosdem-dl.validators-test)

(def test-results
  (t/run-tests 'fosdem-dl.scraping-test
               'fosdem-dl.validators-test))

(def failures-and-errors
  (let [{:keys [:fail :error]} test-results]
    (+ fail error)))

(System/exit failures-and-errors)
