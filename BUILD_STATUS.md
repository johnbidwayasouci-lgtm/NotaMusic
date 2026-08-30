# build status

Date: 2026-08-30

## état

Fondation Android native créée sur la branche `android-foundation`.

- Kotlin + Jetpack Compose : configurés.
- Package indépendant : `com.notationstudio.app`.
- Modèle musical extensible : créé.
- Contrats de domaine : créés.
- Navigation : créée pour Home, CreateScore, OpenScores, EditScore, StaffConfiguration, Settings, MetadataEditor et ShareScore.
- Surface de rendu personnalisée Android View/Canvas : créée.
- Transaction de création : fondation créée.
- Tests unitaires : créés.
- CI GitHub Actions : configurée pour Java 17, Gradle 8.9, tests et APK debug.

## build local

```bash
gradle testDebugUnitTest
gradle assembleDebug
```

## apk

Chemin attendu après compilation :

`app/build/outputs/apk/debug/app-debug.apk`

## vérification

La compilation locale n'est pas disponible dans l'environnement courant car le SDK Android/Gradle n'y est pas installé. La vérification de compilation et de démarrage est donc déléguée au workflow GitHub Actions ajouté dans `.github/workflows/android.yml`.

## suite

Le moteur musical complet, MusicXML/MIDI réel, persistance durable, playback, rendu avancé, undo/redo et implémentation détaillée des écrans seront ajoutés par étapes. Cette phase ne doit pas anticiper leur complexité.
