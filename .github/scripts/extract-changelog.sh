#!/usr/bin/env bash
# Print the CHANGELOG.md body for version X.Y.Z (section under "## X.Y.Z").
# Usage: extract-changelog.sh 1.0.1
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
VERSION="${1:?version X.Y.Z required}"
CHANGELOG="${ROOT}/CHANGELOG.md"

if [[ ! -f "$CHANGELOG" ]]; then
  echo "::error::Missing CHANGELOG.md — add ## ${VERSION} before cutting a release."
  exit 1
fi

if ! echo "$VERSION" | grep -Eq '^[0-9]+\.[0-9]+\.[0-9]+$'; then
  echo "::error::Invalid version ${VERSION}"
  exit 1
fi

# Extract from "## 1.0.1" through the line before the next "## " version heading.
awk -v ver="$VERSION" '
  BEGIN { wanted = "## " ver; capturing = 0 }
  $0 ~ /^## [0-9]+\.[0-9]+\.[0-9]+/ {
    if (capturing) { exit }
    if (index($0, wanted) == 1) { capturing = 1; next }
  }
  capturing { print }
' "$CHANGELOG" | sed -e 's/[[:space:]]*$//' -e '/./,$!d' | awk '
  NF { nonempty = 1 }
  nonempty { print }
' > /tmp/racktrack-changelog-section.txt

if [[ ! -s /tmp/racktrack-changelog-section.txt ]]; then
  echo "::error::No CHANGELOG.md section for ## ${VERSION}. Add Features / Bug fixes before release."
  exit 1
fi

cat /tmp/racktrack-changelog-section.txt
