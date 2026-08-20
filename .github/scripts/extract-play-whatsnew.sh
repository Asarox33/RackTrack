#!/usr/bin/env bash
# Extract <fr-FR> / <en-US> blocks from play/internal-release-notes.txt into
# play/whatsnew/whatsnew-{locale} for r0adkll/upload-google-play.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
SRC="${1:-$ROOT/play/internal-release-notes.txt}"
OUT="${2:-$ROOT/play/whatsnew}"

if [[ ! -f "$SRC" ]]; then
  echo "::error::Missing release notes file: $SRC"
  exit 1
fi

mkdir -p "$OUT"

extract_locale() {
  local locale="$1"
  local dest="$OUT/whatsnew-${locale}"
  # shellcheck disable=SC2016
  awk -v loc="$locale" '
    $0 == "<" loc ">" { grab=1; next }
    $0 == "</" loc ">" { grab=0; next }
    grab { print }
  ' "$SRC" | sed -e 's/[[:space:]]*$//' | sed -e '/./,$!d' > "$dest"
  # trim trailing blank lines
  if [[ ! -s "$dest" ]]; then
    echo "::error::Empty notes for locale ${locale} in $SRC"
    exit 1
  fi
  echo "Wrote $dest ($(wc -c < "$dest") bytes)"
}

extract_locale "fr-FR"
extract_locale "en-US"
