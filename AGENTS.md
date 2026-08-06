# AGENTS.md

Project-wide guidance for AI agents. Also read `.cursor/rules.md` and `docs/`.

## Cursor Cloud specific instructions

### Product (MVP v0.1)

Landscape-only **10-ball race board**: split screen, blue cloth, per-player **+1 / Run out / Foul**,
break indicator, who breaks first, undo. No per-shot ball/pocket entry.

### Toolchain

- JDK **25** (`JAVA_HOME=~/tools/jdk-25`), Gradle **9.6.1** wrapper, Android SDK **36**
- AGP **9.3** has built-in Kotlin — do **not** apply `org.jetbrains.kotlin.android`; keep Compose compiler plugin
- Under Gradle 9, keep `testRuntimeOnly` JUnit Platform launcher

### Commands

```bash
./gradlew testDebugUnitTest
./gradlew ktlintCheck detekt
./gradlew :app:assembleDebug
```

No emulator/GUI in this VM — validate with unit tests + APK assemble/`apkanalyzer`.
Activity is locked to `sensorLandscape`.
