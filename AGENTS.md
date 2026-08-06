# AGENTS.md

Project-wide guidance for AI agents working in this repository. Also read `.cursor/rules.md`
and the `docs/` folder — they define product scope, architecture, and enforced conventions.

## Cursor Cloud specific instructions

### What this repo currently is

RackTrack is in the **framing phase**: only documentation (`docs/`), `resources/`, and a CI
skeleton (`.github/workflows/ci.yml`) exist. There is **no Android/Gradle project, no build
files, and no application source code yet** (see `README.md` and `docs/06-roadmap-todo.md`,
task `#SETUP-1`). Because of this:

- There is **no runnable app and no `./gradlew` wrapper yet**. The CI commands
  (`./gradlew ktlintCheck`, `./gradlew detekt`, `./gradlew testDebugUnitTest`) intentionally
  fail until the project is scaffolded — this is expected, per the comment in `ci.yml`.
- Scaffolding the Gradle/Android project is a *development* task (`#SETUP-1`..`#SETUP-5`),
  not an environment task. Do not create it unless the user asks.

### Target toolchain (pin these when scaffolding `#SETUP-1`)

| Component | Target | Notes |
|---|---|---|
| **JDK** | **25** (Temurin LTS) | Matches CI `java-version: '25'`. Use as the *build* JDK. |
| **Kotlin** | **2.4.10** (latest stable as of 2026-07) | Prefer Version Catalog pin in `gradle/libs.versions.toml`. |
| **Gradle** | **9.6.1** | Latest stable Gradle 9.x. Commit a wrapper at this version. |
| **Android Gradle Plugin** | **9.3.x** | Needs Gradle ≥ 9.5.0. AGP 9 has **built-in Kotlin** — do **not** apply `org.jetbrains.kotlin.android`; keep `org.jetbrains.kotlin.plugin.compose` for Compose. |
| **compileSdk / targetSdk** | **36** | Google Play requires API 36 for new apps/updates from 2026-08-31. |
| **build-tools** | **36.0.0** | Matches AGP 9.3 default. |
| **minSdk** | still TBD in `#SETUP-1` (24 is a reasonable floor) | Play’s requirement is about *target*, not min. |

### Toolchain available in the VM image

The following are preinstalled in the VM (not via the startup update script) and exported from
`~/.bashrc`, so a login shell finds them:

- **JDK 25.0.4** (Temurin) at `~/tools/jdk-25` → `JAVA_HOME`
- **Gradle 9.6.1** at `~/tools/gradle-9.6.1` (on `PATH` as `gradle`). Real projects should
  still use the committed `./gradlew` wrapper once it exists; this standalone `gradle` is for
  bootstrapping / running builds before a wrapper is generated.
- **Android SDK** at `~/android-sdk` (`ANDROID_HOME`/`ANDROID_SDK_ROOT`): `platform-tools`,
  `platforms;android-36` (+ leftover 34 from earlier setup), `build-tools;36.0.0` (+ leftover
  34), plus `cmdline-tools/latest` (`sdkmanager`, `avdmanager`, `apkanalyzer`) and
  `platform-tools` (`adb`) on `PATH`. All SDK licenses are accepted.

If you add a Gradle project, point it at the SDK via `ANDROID_HOME` (already set) or a
`local.properties` with `sdk.dir=$HOME/android-sdk`. Keep `local.properties` untracked.

### Building / testing / running

- Standard commands once the project exists are the CI ones above; see
  `.github/workflows/ci.yml` and `docs/05-conventions.md` — do not duplicate them elsewhere.
- **Domain-layer unit tests run on the JVM with no emulator** (`domain/` must never depend on
  Android — see `docs/04-architecture.md`). This is the highest-priority, fastest-feedback
  test layer; prefer it. Under Gradle 9, add
  `testRuntimeOnly("org.junit.platform:junit-platform-launcher")` (or the Version Catalog
  equivalent) so JUnit Platform can start.
- **No Android emulator / GUI is available in this VM.** You can compile the app and assemble
  an APK (`./gradlew :app:assembleDebug`) and inspect it with `apkanalyzer`, but you cannot
  launch the UI on a device/emulator here. Validate logic through JVM unit tests instead.
