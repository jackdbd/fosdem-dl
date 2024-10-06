#!/usr/bin/env bash

SCRIPT_DIR=$(cd $(dirname $0); pwd)
bb --classpath "$SCRIPT_DIR/src" --main fosdem-dl.cli -- $@
