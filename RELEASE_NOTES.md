# NotaMusic 0.1.0

## Status

This release candidate is the current Android distribution baseline. The application uses the independent package `org.notamusic.app`, namespace `org.notamusic.app`, versionCode 1 and versionName 0.1.0.

## Included

- Kotlin Android application foundation.
- Native navigation and editor architecture.
- Musical score domain model.
- Local score persistence foundation.
- MusicXML and MIDI domain/export foundations.
- Score rendering and editing foundations.
- Settings and recovery architecture.

## Compatibility

- compileSdk 35
- targetSdk 35
- minSdk 24
- Java/Kotlin target 17

## Known limitations

The repository still requires final device-level validation for release claims, including real Android audio playback, complete complex MusicXML round-trip coverage, full-resolution image export, Sharesheet verification, rotation/process-death testing, and final visual conformity testing.

The release build is currently unsigned unless a local release signing configuration is provided. Do not commit signing keys.

## Installation

For a debug APK, install `app/build/outputs/apk/debug/app-debug.apk` with Android Studio or `adb install -r` on a compatible device.

For a signed release APK, use the release signing configuration kept outside source control.

## Build

See `BUILD_GUIDE.md` for reproducible build and test commands.
