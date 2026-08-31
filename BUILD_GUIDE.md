# NotaMusic build guide

## Prerequisites

- Android SDK 35.
- JDK 17.
- Gradle 8.10.2 (the CI installs this exact version).

The repository currently does not contain a Gradle wrapper, so local commands use an installed Gradle 8.10.2 binary.

## Verification build

```bash
gradle --version
gradle test
gradle lint
gradle :app:assembleDebug
gradle :app:assembleRelease
```

## APK paths

Debug:
`app/build/outputs/apk/debug/app-debug.apk`

Release:
`app/build/outputs/apk/release/app-release-unsigned.apk`

The release APK is unsigned unless a signing configuration is supplied outside source control. Do not commit keystores or private keys.

## Android device tests

With a connected emulator/device and an instrumented-test module present:

```bash
gradle connectedAndroidTest
```

## Clean build

```bash
gradle clean test lint :app:assembleDebug :app:assembleRelease
```

## CI

GitHub Actions uses JDK 17 and Gradle 8.10.2 and publishes both debug and release APK artifacts after successful compilation.
