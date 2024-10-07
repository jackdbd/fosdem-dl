#!/usr/bin/env bash
set -euo pipefail

# https://github.com/jackdbd/pod-jackdbd-jsoup/releases

POD_ID=pod.jackdbd.jsoup
POD_NAME=pod-jackdbd-jsoup
POD_VERSION=$POD_JACKDBD_JSOUP_VERSION
ARCH=amd64
OS=ubuntu-latest

echo "Download assets published on GitHub release"
UBERJAR="$POD_ID-$POD_VERSION-standalone.jar"
ZIP_BINARY="$POD_NAME-$POD_VERSION-$OS-$ARCH.zip"

gh release download "v$POD_VERSION" \
  --clobber \
  --dir resources/pod \
  --repo jackdbd/pod-jackdbd-jsoup \
  --pattern "$UBERJAR" \
  --pattern "$ZIP_BINARY"

pushd . && cd resources/pod && unzip -o "$ZIP_BINARY" && popd

chmod +x "resources/pod/$POD_NAME"

rm "resources/pod/$ZIP_BINARY"
