# build status

status: **passed**

project: NotaMusic
application id: `org.notamusic.app`
language: Kotlin
ui: Android Views + custom Canvas editor foundation

## verified build

GitHub Actions workflow `android-build` run `33353477335` completed the `gradle :app:assembleDebug` step successfully and uploaded the debug APK artifact.

command:

```bash
gradle :app:assembleDebug
```

CI Gradle version: `8.10.2`
JDK: `17`
compileSdk: `35`
minSdk: `24`
targetSdk: `35`

APK path inside the CI workspace:

`app/build/outputs/apk/debug/app-debug.apk`

Artifact: `NotaMusic-debug-apk.zip`
Artifact contains: `app-debug.apk`
Artifact size: 5,034,366 bytes
SHA-256 of the artifact archive: `77a0b9cf0f78afb57df654cf3ec757c1cdad083ebcd6098c1acfa8beaea1cc31`

## scope of this milestone

- independent package/signing namespace
- explicit navigation destination model
- Android screen skeleton for Home, CreateScore, OpenScores, EditScore, StaffConfiguration, Settings, MetadataEditor and ShareScore
- domain model for score, part, staff, measure, voices and musical elements
- repository, persistence, file, rendering, MusicXML, MIDI, playback and settings interfaces
- custom Canvas score-editor foundation
- validation and score factory foundations
- unit-test foundations
- GitHub Actions build and APK artifact publishing

## intentionally deferred

The full notation engine, editing gestures, complete MusicXML import/export, real MIDI generation/playback, persistent score serialization, undo/redo, transactional creation wizard, rotation state restoration, loss-prevention dialogs, staff configuration UI, share/export UI and pixel-level visual parity are intentionally not implemented in this architecture milestone.

The generated APK is therefore a **functional architectural foundation**, not yet the finished reimplementation of Ensemble Composer.
