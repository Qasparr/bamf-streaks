#!/usr/bin/env bash
# Manual build for BAMF Streaks — no Gradle required.
# Needs: JDK 17, Android SDK with platforms;android-34 and build-tools;34.0.0
# Usage: ANDROID_HOME=/path/to/sdk ./build.sh
set -euo pipefail

: "${ANDROID_HOME:?set ANDROID_HOME to your Android SDK}"
BT="$ANDROID_HOME/build-tools/34.0.0"
AJAR="$ANDROID_HOME/platforms/android-34/android.jar"
JAVA_HOME="${JAVA_HOME:-$(dirname "$(dirname "$(readlink -f "$(command -v javac)")")")}"
export PATH="$JAVA_HOME/bin:$PATH"

PROJ="$(cd "$(dirname "$0")" && pwd)/app/src/main"
OUT="$(cd "$(dirname "$0")" && pwd)/out"
rm -rf "$OUT"
mkdir -p "$OUT/classes" "$OUT/dex"

echo "--- aapt2 link"
"$BT/aapt2" link -o "$OUT/base.apk" -I "$AJAR" \
  --manifest "$PROJ/AndroidManifest.xml" \
  -A "$PROJ/assets" \
  --min-sdk-version 26 --target-sdk-version 34

echo "--- javac"
find "$PROJ/java" -name '*.java' > "$OUT/srcs.txt"
javac -encoding UTF-8 -source 17 -target 17 \
  -cp "$AJAR" -d "$OUT/classes" @"$OUT/srcs.txt"

echo "--- d8"
# shellcheck disable=SC2046
"$BT/d8" --lib "$AJAR" --min-api 26 --output "$OUT/dex" \
  $(find "$OUT/classes" -name '*.class')

echo "--- package"
(cd "$OUT/dex" && "$BT/aapt" add -k "$OUT/base.apk" classes.dex >/dev/null)
"$BT/zipalign" -f 4 "$OUT/base.apk" "$OUT/aligned.apk"

echo "--- sign (generates a throwaway key if none exists)"
KS="$OUT/debug.keystore"
if [ ! -f "$KS" ]; then
  keytool -genkeypair -keystore "$KS" -alias bamf -keyalg RSA -keysize 2048 \
    -validity 10950 -storepass android -dname "CN=BAMF Streaks" >/dev/null 2>&1
fi
"$BT/apksigner" sign --ks "$KS" --ks-pass pass:android \
  --out "$OUT/BAMF-Streaks.apk" "$OUT/aligned.apk"
"$BT/apksigner" verify "$OUT/BAMF-Streaks.apk"

echo "OK: $OUT/BAMF-Streaks.apk"
