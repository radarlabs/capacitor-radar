#!/bin/bash
set -e
# Usage: download-xcframeworks.sh [SDK_VERSION] [FRAUD_VERSION]
# For versions other than the defaults, also set SDK_SHA256 or FRAUD_SHA256
# to the SHA-256 of the release zip. The defaults always use their own hashes.
DEFAULT_SDK_VERSION="4.0.0"
DEFAULT_SDK_SHA256="979e70c1253ad2da8743074215fd1639e939d57297a8a1e53e570dfb3c28480d"
DEFAULT_FRAUD_VERSION="1.6.0"
DEFAULT_FRAUD_SHA256="44346d9a8e67b473c7042750a122f29b55b0d03c0f5291948239d833c36e7bf4"

SDK_VERSION="${1:-$DEFAULT_SDK_VERSION}"
FRAUD_VERSION="${2:-$DEFAULT_FRAUD_VERSION}"

if [ "$SDK_VERSION" = "$DEFAULT_SDK_VERSION" ]; then
  SDK_SHA256="$DEFAULT_SDK_SHA256"
elif [ -z "${SDK_SHA256:-}" ]; then
  echo "ERROR: set SDK_SHA256 for RadarSDK ${SDK_VERSION}"; exit 1
fi
if [ "$FRAUD_VERSION" = "$DEFAULT_FRAUD_VERSION" ]; then
  FRAUD_SHA256="$DEFAULT_FRAUD_SHA256"
elif [ -z "${FRAUD_SHA256:-}" ]; then
  echo "ERROR: set FRAUD_SHA256 for RadarSDKFraud ${FRAUD_VERSION}"; exit 1
fi
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
DEST="$PROJECT_ROOT/ios/Frameworks"

# Download and check both zips in a private temp directory first. Replace
# $DEST only when both are good, so a failed run keeps the current frameworks.
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT
mkdir -p "$WORK/out"

fetch () { # name url expected_sha
  local name="$1" url="$2" sha="$3" zip="$WORK/$1.zip"
  curl -fsSL -o "$zip" "$url"
  local actual; actual=$(shasum -a 256 "$zip" | awk '{print $1}')
  [ "$actual" = "$sha" ] || { echo "ERROR: $name sha mismatch (expected $sha, got $actual)"; exit 1; }
  unzip -q "$zip" -d "$WORK/out/"
}

fetch "RadarSDK" \
  "https://github.com/radarlabs/radar-sdk-ios/releases/download/${SDK_VERSION}/RadarSDK.xcframework.zip" \
  "$SDK_SHA256"
fetch "RadarSDKFraud" \
  "https://github.com/radarlabs/radar-sdk-ios-fraud-spm/releases/download/${FRAUD_VERSION}/RadarSDKFraud.xcframework.zip" \
  "$FRAUD_SHA256"

rm -rf "$DEST" && mkdir -p "$DEST"
cp -R "$WORK/out/." "$DEST/"

echo "Done: RadarSDK@${SDK_VERSION} + RadarSDKFraud@${FRAUD_VERSION} in ${DEST}/"
