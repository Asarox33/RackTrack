#!/usr/bin/env bash
# Decode upload keystore from GitHub Actions secrets into keystore.properties + .p12
# Required secrets:
#   RACKTRACK_KEYSTORE_BASE64
#   RACKTRACK_KEYSTORE_PASSWORD
#   RACKTRACK_KEY_ALIAS
#   RACKTRACK_KEY_PASSWORD
set -euo pipefail

: "${RACKTRACK_KEYSTORE_BASE64:?missing}"
: "${RACKTRACK_KEYSTORE_PASSWORD:?missing}"
: "${RACKTRACK_KEY_ALIAS:?missing}"
: "${RACKTRACK_KEY_PASSWORD:?missing}"

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

echo "$RACKTRACK_KEYSTORE_BASE64" | base64 -d > racktrack-upload.p12

cat > keystore.properties <<EOF
storeFile=racktrack-upload.p12
storePassword=${RACKTRACK_KEYSTORE_PASSWORD}
keyAlias=${RACKTRACK_KEY_ALIAS}
keyPassword=${RACKTRACK_KEY_PASSWORD}
EOF

echo "Wrote racktrack-upload.p12 and keystore.properties"
