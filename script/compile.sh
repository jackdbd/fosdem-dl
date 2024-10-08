#!/usr/bin/env bash
set -euo pipefail

BINARY_NAME=fosdem-dl
UBERJAR_NAME=fosdem-dl
# GROUP_ID=com.github.jackdbd
VERSION=0.1.0-RC.1 # see deps.edn
# This a "regular" uberjar, not a Babashka uberjar
UBERJAR_PATH="target/$UBERJAR_NAME-$VERSION-standalone.jar"
echo "UBERJAR_PATH is $UBERJAR_PATH"

## Memory management ###########################################################
# Serial GC is the only GC available in GraalVM Community Edition.
GARBAGE_COLLECTOR="--gc=serial"
# https://www.graalvm.org/latest/reference-manual/native-image/optimizations-and-performance/MemoryManagement/#performance-tuning
HEAP_SIZE_AT_BUILD_TIME="-R:MaximumHeapSizePercent=50"

# CPU optimizations ############################################################
# https://www.graalvm.org/latest/reference-manual/native-image/optimizations-and-performance/#optimization-levels
# -Ob: quicker build time
# -O2: better performance
OPTIMIZATION_LEVEL="-O2"

# https://www.graalvm.org/latest/reference-manual/native-image/optimizations-and-performance/#optimizing-for-specific-machines
MACHINE_TYPE="-march=x86-64-v3"

# native-image does NOT support cross-compilation.
# https://github.com/oracle/graal/issues/407
ARCH=amd64
OS=linux
TARGET="--target=$OS-$ARCH"

## Native Image Builder ########################################################
# https://www.graalvm.org/22.0/reference-manual/native-image/Options/
# https://docs.oracle.com/en/graalvm/enterprise/22/docs/reference-manual/native-image/overview/BuildOptions/

# TODO: check that pod-jackdbd-jsoup is bundled in the generated GraalVM native-image

  # --initialize-at-build-time \
  # --initialize-at-build-time=java.lang.ProcessImpl \
  # --trace-object-instantiation=java.lang.ProcessImpl \
    # --initialize-at-run-time=fosdem_dl.cli \

native-image -jar $UBERJAR_PATH $BINARY_NAME \
  -H:ResourceConfigurationFiles=resource-config.json \
  -H:+UnlockExperimentalVMOptions \
  $GARBAGE_COLLECTOR \
  $HEAP_SIZE_AT_BUILD_TIME \
  $OPTIMIZATION_LEVEL \
  $MACHINE_TYPE \
  $TARGET \
  --initialize-at-run-time=fosdem_dl.cli \
  --native-image-info \
  --no-fallback \
  --static --libc=musl \
  --verbose

if [ "${CI+x}" ]; then
  # We are on GitHub actions
  echo "We are on GitHub actions"
  echo "Binary artifact is at $IMAGE_NAME"
else
  echo "We are NOT on GitHub actions"
  mv "$BINARY_NAME" "target/$BINARY_NAME"
  echo "Binary artifact moved to target/$BINARY_NAME"
fi

# DEBUG TIP: you can use these flags when RUNNING the binary (not when compiling it).
# https://www.graalvm.org/latest/reference-manual/native-image/optimizations-and-performance/MemoryManagement/#printing-garbage-collections
