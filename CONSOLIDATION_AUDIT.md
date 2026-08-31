# NotaMusic — consolidation audit

Date: 2026-08-31

## Executive finding

The repository is a useful Android prototype foundation, but the current `main` tree is not yet a finished composition product. Several earlier implementation commits exist in Git history, while the current app tree still contains placeholder adapters for MusicXML, MIDI, playback, persistence, and score rendering. The consolidation priority is therefore integration and replacement of these placeholders, not another feature layer.

## Verified current architecture

- Android module: `:app` only.
- Package/namespace: `org.notamusic.app`.
- compileSdk/targetSdk: 35.
- minSdk: 24.
- Java/Kotlin target: 17.
- The repository contains domain model, notation, UI/editor, persistence, MIDI, MusicXML, playback and rendering packages.
- `settings.gradle.kts` currently includes only `:app`.
- An orphan `core/notation` implementation exists in Git but is not a Gradle module and is not consumed by `:app`.

## Critical gaps found

### 1. persistence integration — BLOCKER

`XmlScorePersistence` currently writes a placeholder comment and `load()` returns null. The production Android repository is therefore not actually persistent.

Required consolidation:
- connect repository save/load/delete to the versioned score serializer;
- atomic temporary-file replacement;
- autosave/recovery files;
- list metadata;
- corruption handling.

### 2. MusicXML — BLOCKER

`PlaceholderMusicXml` in the app module returns an empty score on import and emits an empty `score-partwise` document on export. A more complete codec exists under `core/notation`, but that code is not wired into the Android module and its API/model differs from the current app model.

Required consolidation:
- implement the codec against the current `org.notamusic.app.domain.model` model;
- preserve metadata, parts/staves, measures, voices, pitch, duration, rests, key/time/clef and supported notation;
- return explicit warnings for unsupported constructs;
- add round-trip tests.

### 3. MIDI — BLOCKER

`PlaceholderMidiRenderer` currently writes only a short MIDI header. It cannot encode musical events.

Required consolidation:
- Standard MIDI File format 1;
- tempo meta event;
- program change;
- note-on/note-off;
- multiple channels/staves;
- mute;
- dynamic-to-velocity mapping;
- deterministic timing.

### 4. playback — BLOCKER

`NoOpPlaybackController` only changes a Boolean and does not emit sound.

Required consolidation:
- Android audio/MIDI playback implementation;
- play/pause/stop state;
- current position;
- mute;
- lifecycle cleanup;
- editor playback cursor.

### 5. rendering — BLOCKER

`ScoreCanvasRenderer.render()` currently returns the score object and `draw()` only renders title and five staff lines. This is not a notation renderer.

Required consolidation:
- consume `ScoreLayoutEngine`;
- clefs, key/time signatures;
- notes/rests/stems/beams;
- dots/accidentals;
- tuplets;
- articulations/ornaments/dynamics;
- ties/slurs/wedges;
- barlines/repeats;
- measure numbers;
- multiple staves and piano grand staff;
- selection and insertion cursor;
- zoom/scroll.

### 6. release CI — corrected

The Android workflow previously built only the debug APK. It now runs unit tests, lint, debug build and release build, then uploads both APK classes as artifacts. This is a consolidation improvement, not proof that the current code passes those jobs.

## Secondary gaps

- Current Gradle dependency set should be reviewed after real implementations are integrated.
- No evidence of instrumented test execution exists in the current repository state.
- No evidence of a real Android device/emulator end-to-end test exists in this environment.
- The application manifest currently forces landscape on activities; this conflicts with the earlier responsive portrait/panorama requirement and needs deliberate correction.
- `core/` code should either be migrated into a real Gradle library module and used, or removed after equivalent functionality is integrated into `app`; keeping two incompatible domain models is a long-term defect risk.
- Native CMake/AI files are currently unrelated to the Android application module and should not be treated as implemented AI functionality.

## Consolidation rule

Do not mark a capability PASS because a class or interface exists. A capability is PASS only after the implementation is connected to the application path and covered by an automated or device-level test appropriate to that capability.

## Current maturity

- Architecture foundation: present.
- UI/editor foundation: present.
- Musical domain foundation: present.
- Persistence production path: not complete.
- MusicXML production path: not complete.
- MIDI production path: not complete.
- Real audio playback: not complete.
- Professional notation rendering: not complete.
- Release validation: not complete.
