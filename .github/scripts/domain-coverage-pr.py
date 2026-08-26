#!/usr/bin/env python3
"""Build a readable Domain coverage PR comment and enforce >=80% gates.

Gates (instruction coverage, same metric as the PR table):
  1. Overall domain report >= 80%
  2. Aggregate coverage of changed lines in domain files >= 80%
  3. Every changed domain file with executable changed lines >= 80% on those lines

Exits 0 on pass, 1 on fail. Always prints the markdown body to stdout.
When GITHUB_TOKEN + PR context are set, upserts a PR comment.
"""

from __future__ import annotations

import json
import os
import re
import subprocess
import sys
import urllib.error
import urllib.request
import xml.etree.ElementTree as ET
from dataclasses import dataclass
from pathlib import Path

# GitHub Actions is UTF-8; keep local Windows consoles from choking on markdown.
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8", errors="replace")

MIN_PCT = 80.0
COMMENT_MARKER = "<!-- racktrack-domain-coverage -->"
XML_PATH = Path("shared/build/reports/jacoco/domainCoverage/domainCoverage.xml")


@dataclass
class FileCov:
    path: str
    name: str
    overall_pct: float
    changed_pct: float | None
    changed_covered: int
    changed_missed: int
    overall_covered: int
    overall_missed: int


def run_git(*args: str) -> str:
    return subprocess.check_output(["git", *args], text=True, encoding="utf-8")


def parse_changed_lines(base: str, head: str) -> dict[str, set[int]]:
    """Map repo-relative path → set of added/changed line numbers on the head side."""
    diff = run_git("diff", "-U0", f"{base}...{head}", "--", "*.kt")
    result: dict[str, set[int]] = {}
    current: str | None = None
    hunk_re = re.compile(r"^@@ -\d+(?:,\d+)? \+(\d+)(?:,(\d+))? @@")
    for line in diff.splitlines():
        if line.startswith("+++ b/"):
            current = line[6:]
            result.setdefault(current, set())
            continue
        if current is None:
            continue
        m = hunk_re.match(line)
        if not m:
            continue
        start = int(m.group(1))
        count = int(m.group(2) or "1")
        if count == 0:
            continue
        for nr in range(start, start + count):
            result[current].add(nr)
    return {p: lines for p, lines in result.items() if lines}


def is_domain_source(path: str) -> bool:
    return "com/racktrack/domain/" in path.replace("\\", "/") and path.endswith(".kt")


def jacoco_key(path: str) -> tuple[str, str] | None:
    """Return (packagePath, fileName) for JaCoCo matching, e.g. (com/racktrack/domain, Match.kt)."""
    norm = path.replace("\\", "/")
    marker = "com/racktrack/domain/"
    idx = norm.find(marker)
    if idx < 0:
        return None
    rest = norm[idx:]
    pkg, _, name = rest.rpartition("/")
    if not name.endswith(".kt"):
        return None
    return pkg, name


def load_jacoco(xml_path: Path) -> tuple[float, dict[tuple[str, str], dict]]:
    """Overall instruction %, and map (pkg, name) → {overall, lines: {nr: (missed, covered)}}."""
    root = ET.parse(xml_path).getroot()
    counters = {c.get("type"): c for c in root.findall("counter")}
    instr = counters.get("INSTRUCTION")
    if instr is None:
        raise SystemExit("No INSTRUCTION counter in JaCoCo report")
    missed = int(instr.get("missed", 0))
    covered = int(instr.get("covered", 0))
    total = missed + covered
    overall = (100.0 * covered / total) if total else 100.0

    files: dict[tuple[str, str], dict] = {}
    for package in root.findall("package"):
        pkg = package.get("name") or ""
        for sf in package.findall("sourcefile"):
            name = sf.get("name") or ""
            line_map: dict[int, tuple[int, int]] = {}
            for line in sf.findall("line"):
                nr = int(line.get("nr", 0))
                mi = int(line.get("mi", 0))
                ci = int(line.get("ci", 0))
                line_map[nr] = (mi, ci)
            oc = next((c for c in sf.findall("counter") if c.get("type") == "INSTRUCTION"), None)
            if oc is None:
                o_missed, o_covered = 0, 0
            else:
                o_missed = int(oc.get("missed", 0))
                o_covered = int(oc.get("covered", 0))
            files[(pkg, name)] = {
                "overall_missed": o_missed,
                "overall_covered": o_covered,
                "lines": line_map,
            }
    return overall, files


def pct(covered: int, missed: int) -> float | None:
    total = covered + missed
    if total == 0:
        return None
    return 100.0 * covered / total


def status_emoji(value: float | None) -> str:
    if value is None:
        return ":white_circle:"
    return ":green_circle:" if value >= MIN_PCT else ":red_circle:"


def fmt_pct(value: float | None) -> str:
    if value is None:
        return "n/a"
    return f"{value:.2f}%"


def build_file_rows(
    changed: dict[str, set[int]],
    jacoco_files: dict[tuple[str, str], dict],
) -> list[FileCov]:
    rows: list[FileCov] = []
    for path, lines in sorted(changed.items()):
        if not is_domain_source(path):
            continue
        key = jacoco_key(path)
        if key is None or key not in jacoco_files:
            continue
        data = jacoco_files[key]
        o_cov, o_miss = data["overall_covered"], data["overall_missed"]
        overall = pct(o_cov, o_miss)
        if overall is None:
            continue
        c_miss = c_cov = 0
        for nr in lines:
            if nr not in data["lines"]:
                continue
            mi, ci = data["lines"][nr]
            c_miss += mi
            c_cov += ci
        changed_p = pct(c_cov, c_miss)
        rows.append(
            FileCov(
                path=path,
                name=key[1],
                overall_pct=overall,
                changed_pct=changed_p,
                changed_covered=c_cov,
                changed_missed=c_miss,
                overall_covered=o_cov,
                overall_missed=o_miss,
            )
        )
    rows.sort(key=lambda r: (r.changed_pct is None, r.changed_pct or 0, r.name))
    return rows


def aggregate_changed(rows: list[FileCov]) -> float | None:
    covered = sum(r.changed_covered for r in rows)
    missed = sum(r.changed_missed for r in rows)
    return pct(covered, missed)


def render_comment(
    overall: float,
    changed_agg: float | None,
    rows: list[FileCov],
    blocking: list[str],
) -> str:
    overall_ok = overall >= MIN_PCT
    changed_ok = changed_agg is None or changed_agg >= MIN_PCT
    files_ok = not any(
        r.changed_pct is not None and r.changed_pct < MIN_PCT for r in rows
    )
    gate_ok = overall_ok and changed_ok and files_ok
    gate_line = (
        f"**CI gate: {'PASS :green_circle:' if gate_ok else 'FAIL :red_circle:'}** - "
        f"threshold **{MIN_PCT:.0f}%** (instruction coverage)."
    )

    lines = [
        COMMENT_MARKER,
        "### Domain coverage",
        "",
        gate_line,
        "",
        "#### How to read this",
        "",
        "- **Overall** - coverage of the whole `:shared` domain report "
        f"(must be >= {MIN_PCT:.0f}%).",
        "- **Changed lines (aggregate)** - coverage only on lines this PR "
        f"adds/modifies in domain sources (must be >= {MIN_PCT:.0f}%).",
        "- **Per-file \"File %\"** - that file's total coverage (context only).",
        "- **Per-file \"Changed %\"** - coverage of **this PR's edits** in that file. "
        "This is what drives :green_circle: / :red_circle:.",
        f"- **:green_circle:** = Changed % >= {MIN_PCT:.0f}% (or no executable changed lines). "
        f"**:red_circle:** = Changed % < {MIN_PCT:.0f}% -> under-tested edits.",
        "",
        "#### Blocking rules",
        "",
        "The job **fails** when any of these is true:",
        f"1. Overall < {MIN_PCT:.0f}%",
        f"2. Aggregate changed-line coverage < {MIN_PCT:.0f}%",
        f"3. **Any** listed domain file has Changed % < {MIN_PCT:.0f}% "
        "(a red row is never informational only)",
        "",
        "Local / class gate (Gradle `domainCoverageVerify`) also enforces "
        ">= 80% **line** coverage overall and per domain class.",
        "",
        "HTML report: CI artifact `domain-coverage` · "
        "`./gradlew :app:domainCoverage`",
        "",
        "| Check | Coverage | Status |",
        "|:--|--:|:--:|",
        f"| Overall project | {fmt_pct(overall)} | {status_emoji(overall)} |",
        f"| Changed lines (all domain files) | {fmt_pct(changed_agg)} | "
        f"{status_emoji(changed_agg if changed_agg is not None else 100.0)} |",
        "",
    ]

    if rows:
        lines.extend(
            [
                "| File | File % | Changed % | Status |",
                "|:--|--:|--:|:--:|",
            ]
        )
        for row in rows:
            # Status follows Changed % (the PR-edit gate), not File %.
            emoji = status_emoji(row.changed_pct if row.changed_pct is not None else 100.0)
            lines.append(
                f"| `{row.name}` | {fmt_pct(row.overall_pct)} | "
                f"{fmt_pct(row.changed_pct)} | {emoji} |"
            )
        lines.append("")
    else:
        lines.append("_No changed domain `.kt` files with JaCoCo data in this PR._")
        lines.append("")

    if blocking:
        lines.append("#### Red files (blocking)")
        lines.append("")
        for item in blocking:
            lines.append(f"- {item}")
        lines.append("")

    return "\n".join(lines)


def upsert_pr_comment(body: str) -> None:
    token = os.environ.get("GITHUB_TOKEN") or os.environ.get("GH_TOKEN")
    repo = os.environ.get("GITHUB_REPOSITORY")
    pr = os.environ.get("PR_NUMBER")
    if not token or not repo or not pr:
        print("Skip PR comment upsert (missing GITHUB_TOKEN / GITHUB_REPOSITORY / PR_NUMBER)")
        return

    api = f"https://api.github.com/repos/{repo}"
    headers = {
        "Accept": "application/vnd.github+json",
        "Authorization": f"Bearer {token}",
        "X-GitHub-Api-Version": "2022-11-28",
        "User-Agent": "racktrack-domain-coverage",
    }

    def request(method: str, url: str, payload: dict | None = None) -> dict | list:
        data = None if payload is None else json.dumps(payload).encode("utf-8")
        req = urllib.request.Request(url, data=data, headers=headers, method=method)
        try:
            with urllib.request.urlopen(req) as resp:
                raw = resp.read().decode("utf-8")
                return json.loads(raw) if raw else {}
        except urllib.error.HTTPError as exc:
            detail = exc.read().decode("utf-8", errors="replace")
            raise SystemExit(f"GitHub API {method} {url} failed: {exc.code} {detail}") from exc

    comments = request("GET", f"{api}/issues/{pr}/comments?per_page=100")
    existing = None
    if isinstance(comments, list):
        for comment in comments:
            if isinstance(comment, dict) and COMMENT_MARKER in (comment.get("body") or ""):
                existing = comment
                break
            # Migrate away from the old madrapps title-only comment.
            body_text = (comment.get("body") or "") if isinstance(comment, dict) else ""
            if body_text.startswith("### Domain coverage\n") and COMMENT_MARKER not in body_text:
                existing = comment
                break

    if existing and isinstance(existing, dict):
        request("PATCH", f"{api}/issues/comments/{existing['id']}", {"body": body})
        print(f"Updated PR comment id={existing['id']}")
    else:
        request("POST", f"{api}/issues/{pr}/comments", {"body": body})
        print("Created PR comment")


def main() -> int:
    if not XML_PATH.is_file():
        print(f"::error::Missing JaCoCo report at {XML_PATH}", file=sys.stderr)
        return 1

    base = os.environ.get("BASE_SHA") or "origin/main"
    head = os.environ.get("HEAD_SHA") or "HEAD"

    overall, jacoco_files = load_jacoco(XML_PATH)
    changed_map = parse_changed_lines(base, head)
    rows = build_file_rows(changed_map, jacoco_files)
    changed_agg = aggregate_changed(rows)

    blocking: list[str] = []
    if overall < MIN_PCT:
        blocking.append(f"Overall {fmt_pct(overall)} < {MIN_PCT:.0f}%")
    if changed_agg is not None and changed_agg < MIN_PCT:
        blocking.append(f"Aggregate changed lines {fmt_pct(changed_agg)} < {MIN_PCT:.0f}%")
    for row in rows:
        if row.changed_pct is not None and row.changed_pct < MIN_PCT:
            blocking.append(
                f"`{row.name}` changed lines {fmt_pct(row.changed_pct)} "
                f"({row.changed_covered} covered / {row.changed_missed} missed instructions)"
            )

    body = render_comment(overall, changed_agg, rows, blocking)
    print(body)

    summary = os.environ.get("GITHUB_STEP_SUMMARY")
    if summary:
        with open(summary, "a", encoding="utf-8") as fh:
            fh.write(body)
            fh.write("\n")

    if os.environ.get("GITHUB_EVENT_NAME") == "pull_request":
        upsert_pr_comment(body)

    if blocking:
        for item in blocking:
            print(f"::error::Domain coverage gate: {item}")
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
