# AGENTS.md

Project-wide guidance for AI agents. Also read `.cursor/rules.md` and `docs/`.

## Cursor Cloud specific instructions

### Product (current)

American pool **race board** (8 / 9 / 10) + **14/1 continuous** (+ **14/1 solo**):
split screen, multi-felt cloth, per-mode actions, break/hand indicator, undo,
end-of-match summary (SHARE + SAVE PDF + BACK), local match history (mode-scoped,
player filters, delete, same stats), PDF share with start/end/duration + versioned
footer, settings (felt incl. Golden, screen on, haptics, defaults, FFB link).
Orientation: `fullSensor`. No per-shot ball/pocket entry.

### Release trains (do not invent)

Read **`docs/00-release-trains.md`** first.

- **1.2.0** — monetization implementation (`docs/09-monetization.md`); first Play production gate
- **2.0.0** on `rc/2.0.0` — i18n, multi-ruleset, KMP/CMP — **not** on the 1.2 Play gate

### Anti-drift (mandatory)

When editing `docs/06-roadmap-todo.md` or related plans:

- **Never silently delete** a backlog item. Mark ✅, move to Explicitly dropped, or
  re-home under another train + add a progress note.
- Monetization / i18n / multi-ruleset / Play gate already bit us once — prefer linking
  to `00` / `08` / `09` over paraphrasing and losing detail.

### Toolchain

- JDK **25** (`JAVA_HOME=~/tools/jdk-25`), Gradle **9.6.1** wrapper, Android SDK **36**
- AGP **9.3** has built-in Kotlin — do **not** apply `org.jetbrains.kotlin.android`; keep Compose compiler plugin
- Under Gradle 9, keep `testRuntimeOnly` JUnit Platform launcher
- Release signing: `keystore.properties` (gitignored) → upload key; else debug fallback
  (`keystore.properties.example`, `docs/08-play-store.md`)
- Play privacy HTTPS: GitHub Pages from `docs/` →
  `https://asarox33.github.io/RackTrack/privacy-policy.html`
- Signed AAB CI: workflow **Signed release AAB** (`workflow_dispatch` or tag `v*`).
  Secrets: `RACKTRACK_KEYSTORE_BASE64`, `RACKTRACK_KEYSTORE_PASSWORD`,
  `RACKTRACK_KEY_ALIAS`, `RACKTRACK_KEY_PASSWORD`. Artifact only — never attach AAB
  to GitHub Releases. See `docs/08-play-store.md`.

### Commands

```bash
./gradlew testDebugUnitTest
./gradlew :app:domainCoverage # JaCoCo HTML/XML for domain/
./gradlew ktlintCheck detekt
./gradlew :app:assembleDebug
./gradlew :app:bundleRelease  # Play AAB when keystore.properties present
```

Prefer domain + `MatchCoordinator` + history JSON/filter unit tests. No Compose screenshot tests.

No emulator/GUI in this VM — validate with unit tests + APK assemble/`apkanalyzer`.

### Releases

Semver `racktrack.versionName` / `versionCode` in `gradle.properties`. After green CI on
`main`, `cut-release.yml` publishes GitHub Release `vX.Y.Z` **notes only** (no APK)
when that version is new. Binaries ship via app stores.
Details: `docs/07-versioning-and-releases.md`.

### License

Source-available (`LICENSE`): others may not redistribute / store-list. **Copyright
holder** may monetize official store builds (ads + IAP). Do not relicense to MIT/Apache
without an explicit owner request.
