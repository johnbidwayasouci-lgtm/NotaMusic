# Build status

status: **validation in progress**

project: NotaMusic
application id: `org.notamusic.app`
current consolidation: **phase 4**

## Current checkpoint

The phase-4 consolidation repaired the previously broken MIDI and MusicXML source files. The MIDI renderer is now a readable standard-MIDI writer, and the MusicXML codec is a readable DOM-based partwise importer/exporter.

The repository must be validated from the current `main` commit before any release artifact is declared ready. The earlier failed run was against an older commit and therefore did not validate these repairs.

## CI environment

- Gradle: `8.10.2`
- JDK: `17`
- compileSdk: `35`
- minSdk: `24`
- targetSdk: `35`

## Release integrity

The existing release signing configuration is preserved. No new keystore is created by this consolidation.

## Important quality gate

A downloadable APK/AAB is considered deliverable only after GitHub Actions completes the current source revision successfully. Compilation alone is not treated as proof of functional parity with Ensemble Composer; the final audit will explicitly separate verified behavior from remaining parity gaps.
