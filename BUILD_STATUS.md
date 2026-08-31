# build status

status: foundation created; CI build verification pending.

project: NotaMusic
application id: `org.notamusic.app`
language: Kotlin
ui: Android Views + custom Canvas editor foundation

## build

```bash
gradle :app:assembleDebug
```

CI uses Gradle 8.10.2 on GitHub Actions.

expected APK: `app/build/outputs/apk/debug/app-debug.apk`

## scope of this milestone

- independent package/signing namespace
- explicit navigation skeleton
- domain model for score/part/staff/measure/voice/elements
- repository and persistence interfaces
- custom score canvas foundation
- compilable Android application target

not implemented yet: full notation editing, MusicXML implementation, MIDI engine, playback, file persistence, import/export, and pixel-level UI parity.
