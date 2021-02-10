#!/usr/bin/env bb

(ns test-runner
  (:require [clojure.test :as t]
            [babashka.classpath :as cp]))

(cp/add-classpath "src:test")

(require 'fosdem-dl.core-test)

(def test-results
  (t/run-tests 'fosdem-dl.core-test))

(def failures-and-errors
  (let [{:keys [:fail :error]} test-results]
    (+ fail error)))

(System/exit failures-and-errors)
