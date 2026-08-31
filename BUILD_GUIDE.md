# NotaMusic build guide

## Prerequisites

- Android Studio with Android SDK 35.
- JDK 17.
- Gradle wrapper supplied by the repository.

## Debug build

```bash
./gradlew :app:assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

## Release build

```bash
./gradlew :app:assembleRelease
```

APK: `app/build/outputs/apk/release/app-release-unsigned.apk` unless a release signing configuration is supplied locally.

## Tests

```bash
./gradlew test
./gradlew lint
```

If an Android device/emulator is available:

```bash
./gradlew connectedAndroidTest
```

## Clean rebuild

```bash
./gradlew clean :app:assembleDebug :app:assembleRelease
```

Never commit keystores, private signing keys, API secrets, or local machine configuration.
