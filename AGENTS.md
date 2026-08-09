# AGENTS.md

Project-wide guidance for AI agents. Also read `.cursor/rules.md` and `docs/`.

## Cursor Cloud specific instructions

### Product (current)

American pool **race board** (8 / 9 / 10) + **14/1 continuous**:
split screen, multi-felt cloth, per-mode actions, break/hand indicator, undo,
end-of-match summary (SHARE PDF + BACK), local match history (mode-scoped,
player filters, delete, same stats), PDF share with start/end/duration,
settings (felt, screen on, haptics, defaults, FFB link).
Orientation: `fullSensor` (landscape + portrait). No per-shot ball/pocket entry.

### Toolchain

- JDK **25** (`JAVA_HOME=~/tools/jdk-25`), Gradle **9.6.1** wrapper, Android SDK **36**
- AGP **9.3** has built-in Kotlin — do **not** apply `org.jetbrains.kotlin.android`; keep Compose compiler plugin
- Under Gradle 9, keep `testRuntimeOnly` JUnit Platform launcher

### Commands

```bash
./gradlew testDebugUnitTest
./gradlew :app:domainCoverage   # JaCoCo HTML/XML for domain/
./gradlew ktlintCheck detekt
./gradlew :app:assembleDebug
```

Prefer domain + `MatchCoordinator` + history JSON/filter unit tests. No Compose screenshot tests.

No emulator/GUI in this VM — validate with unit tests + APK assemble/`apkanalyzer`.
