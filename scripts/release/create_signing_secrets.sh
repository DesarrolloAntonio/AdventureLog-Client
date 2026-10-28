#!/bin/bash
# Uploads the four CI signing secrets from the existing upload keystore (the one that signed the
# AAB in the Play draft). Copies the keystore into signing/ first - gitignored, inside the project
# so the Mac's Kopia backup picks it up - and refuses to upload unless its certificate matches the
# Play AAB's. The password is read from pass.txt and never printed.
set -euo pipefail

REPO="DesarrolloAntonio/AdventureLog-Client"
PROJECT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
SOURCE_DIR="$PROJECT_DIR/../KeyStore"
SIGNING_DIR="$PROJECT_DIR/signing"
PLAY_AAB="$PROJECT_DIR/composeApp/release/composeApp-release.aab"
ALIAS="key0"

mkdir -p "$SIGNING_DIR"
chmod 700 "$SIGNING_DIR"
cp -n "$SOURCE_DIR"/key_store.jks "$SOURCE_DIR"/pass.txt "$SOURCE_DIR"/private_key.pepk "$SIGNING_DIR"/
chmod 600 "$SIGNING_DIR"/*
KEYSTORE="$SIGNING_DIR/key_store.jks"

PASSWORD="$(head -n 1 "$SIGNING_DIR/pass.txt" | tr -d '\r\n')"

KEY_SHA="$(keytool -list -v -keystore "$KEYSTORE" -alias "$ALIAS" -storepass "$PASSWORD" \
    | grep 'SHA256:' | head -n 1 | awk '{print $2}')"
AAB_SHA="$(keytool -printcert -jarfile "$PLAY_AAB" | grep 'SHA256:' | head -n 1 | awk '{print $2}')"
if [ -z "$KEY_SHA" ] || [ "$KEY_SHA" != "$AAB_SHA" ]; then
    echo "The keystore's certificate does not match the Play AAB's - nothing uploaded." >&2
    echo "  keystore: ${KEY_SHA:-<unreadable - wrong password?>}" >&2
    echo "  Play AAB: $AAB_SHA" >&2
    exit 1
fi
echo "Keystore matches the Play AAB ($KEY_SHA)."

base64 -i "$KEYSTORE" | gh secret set KEYSTORE_BASE64 -R "$REPO"
printf '%s' "$PASSWORD" | gh secret set RELEASE_STORE_PASSWORD -R "$REPO"
printf '%s' "$PASSWORD" | gh secret set RELEASE_KEY_PASSWORD -R "$REPO"
printf '%s' "$ALIAS" | gh secret set RELEASE_KEY_ALIAS -R "$REPO"

gh secret list -R "$REPO"
