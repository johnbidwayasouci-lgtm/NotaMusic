# NotaMusic — project status

Updated: 2026-08-31

## Current milestone
Persistence, MusicXML, MIDI and playback integration.

## Completed
- Independent Kotlin Android architecture
- Musical domain model and Fraction-based timing foundation
- Score editor UI/navigation foundation
- Versioned local score format
- Atomic file repository boundary
- Full ScoreCodec reconstruction for persisted scores
- Persistence round-trip unit test
- Bidirectional MusicXML codec foundation
- Standard MIDI file renderer foundation

## In progress
- Android storage integration
- Open screen wired to repository
- Autosave and recovery lifecycle integration
- MusicXML round-trip validation across representative scores
- MIDI validation across voices/instruments
- PlaybackController integration with Android audio output
- End-to-end create/save/close/reopen/export/play scenario

## Not yet validated
- Full Android process-death recovery
- Full MusicXML compatibility matrix
- Full MIDI playback fidelity
- Final installable APK regression run

## Live progress source
Git history and CI workflow are the source of truth for implementation progress. A web dashboard can consume this status file and GitHub Actions results.
