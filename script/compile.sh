#!/usr/bin/env bash
set -euo pipefail

BINARY_NAME=fosdem-dl
UBERJAR_NAME=fosdem-dl
# GROUP_ID=com.github.jackdbd
VERSION=0.1.0-RC.1 # see deps.edn
# UBERJAR_PATH="target/$UBERJAR_NAME-$VERSION-standalone.jar" # normal uberjar
UBERJAR_PATH="target/$UBERJAR_NAME-$VERSION.jar" # babashka uberjar
echo "UBERJAR_PATH is $UBERJAR_PATH"

HEAP_SIZE_AT_BUILD_TIME="-R:MaxHeapSize=1024m"

# https://www.graalvm.org/latest/reference-manual/native-image/optimizations-and-performance/#optimization-levels
# -Ob: quicker build time
# -O2: better performance
OPTIMIZATION_LEVEL="-O2"

# https://www.graalvm.org/latest/reference-manual/native-image/optimizations-and-performance/#optimizing-for-specific-machines
MACHINE_TYPE="-march=x86-64-v3"

# native-image does NOT support cross-compilation.
# https://github.com/oracle/graal/issues/407
TARGET="linux-amd64"

# TODO: bundle pod-jackdbd-jsoup when building with GraalVM native-image

native-image -jar $UBERJAR_PATH $BINARY_NAME \
  -H:ResourceConfigurationFiles=resource-config.json \
  -H:+UnlockExperimentalVMOptions \
  $HEAP_SIZE_AT_BUILD_TIME \
  $OPTIMIZATION_LEVEL \
  $MACHINE_TYPE \
  --initialize-at-build-time \
  --native-image-info \
  --no-fallback \
  --report-unsupported-elements-at-runtime \
  --static --libc=musl \
  "--target=$TARGET"
  # --verbose

if [ "${CI+x}" ]; then
  # We are on GitHub actions
  echo "We are on GitHub actions"
else
  echo "We are NOT on GitHub actions"
fi
