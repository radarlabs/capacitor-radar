#!/bin/bash
set -e
SDK_VERSION="${1:-3.38.0}"
FRAUD_VERSION="${2:-1.4.1}"
SDK_SHA256="8ee64edcd7b4391e90a0302bfcf158d717a5b337834bfded8f9b7097c5612b75"
FRAUD_SHA256="426a38a2cfea558c7114bfcf046507c70f67d2f72e6689dfa3bb5c4d74cca0fb"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
DEST="$PROJECT_ROOT/ios/Frameworks"

rm -rf "$DEST" && mkdir -p "$DEST"

fetch () { # name url expected_sha
  local name="$1" url="$2" sha="$3" zip="/tmp/$1.zip"
  curl -fsSL -o "$zip" "$url"
  local actual; actual=$(shasum -a 256 "$zip" | awk '{print $1}')
  [ "$actual" = "$sha" ] || { echo "ERROR: $name sha mismatch (expected $sha, got $actual)"; exit 1; }
  unzip -q "$zip" -d "$DEST/"
}

fetch "RadarSDK" \
  "https://github.com/radarlabs/radar-sdk-ios/releases/download/${SDK_VERSION}/RadarSDK.xcframework.zip" \
  "$SDK_SHA256"
fetch "RadarSDKFraud" \
  "https://github.com/radarlabs/radar-sdk-ios-fraud-spm/releases/download/${FRAUD_VERSION}/RadarSDKFraud.xcframework.zip" \
  "$FRAUD_SHA256"

echo "Done: RadarSDK@${SDK_VERSION} + RadarSDKFraud@${FRAUD_VERSION} in ${DEST}/"