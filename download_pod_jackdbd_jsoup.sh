#!/usr/bin/env bash
set -euo pipefail

# https://github.com/jackdbd/pod-jackdbd-jsoup/releases

POD_ID=pod.jackdbd.jsoup
POD_NAME=pod-jackdbd-jsoup
POD_VERSION=$POD_JACKDBD_JSOUP_VERSION
ARCH=amd64
OS=ubuntu-latest
BABASHKA_PODS_DIR_DEFAULT=$HOME/.babashka/pods
BABASHKA_PODS_DIR=${BABASHKA_PODS_DIR:-$BABASHKA_PODS_DIR_DEFAULT}
UBERJAR="$POD_ID-$POD_VERSION-standalone.jar"
ZIP_BINARY="$POD_NAME-$POD_VERSION-$OS-$ARCH.zip"

echo "Download assets published on GitHub release to $BABASHKA_PODS_DIR"
gh release download "v$POD_VERSION" \
  --clobber \
  --dir $BABASHKA_PODS_DIR \
  --repo jackdbd/pod-jackdbd-jsoup \
  --pattern "$UBERJAR" \
  --pattern "$ZIP_BINARY"

pushd . && cd $BABASHKA_PODS_DIR && unzip -o "$ZIP_BINARY" && popd

chmod +x "$BABASHKA_PODS_DIR/$POD_NAME"

rm "$BABASHKA_PODS_DIR/$ZIP_BINARY"
