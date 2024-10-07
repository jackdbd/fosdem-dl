#!/usr/bin/env bash
set -euo pipefail

APP_NAME=fosdem-dl
GROUP_ID=com.github.jackdbd
# "regular" uberjar
UBERJAR_PATH="target/$APP_NAME-$APP_VERSION-standalone.jar"
# babashka uberjar
# UBERJAR_PATH="target/$APP_NAME-$APP_VERSION.jar"
echo "UBERJAR_PATH is $UBERJAR_PATH"

# Entry point of the GraalVM native-image documentation.
# https://www.graalvm.org/latest/reference-manual/native-image/
# https://www.graalvm.org/latest/reference-manual/native-image/overview/BuildOutput/

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

# clojure -M -e "(compile 'fosdem-dl.cli)"

# native-image --version

# CLASSPATH=$(clojure -Spath)
# echo "CLASSPATH is $CLASSPATH"
# clojure -Scp $CLASSPATH -M -e "(compile '$APP_NAME.cli)"

native-image \
  -cp "$(clojure -Spath)" \
  -jar $UBERJAR_PATH \
  -H:Name=$APP_NAME \
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
