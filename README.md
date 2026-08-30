# Notation Studio — Android

Réimplémentation indépendante en Kotlin d'une application de notation musicale. Le projet ne réutilise ni code source, ni signature, ni package, ni identifiants publicitaires, ni éléments propriétaires de l'application de référence.

## architecture

- `domain/model` : modèle musical indépendant de l'UI (`Score`, `Part`, `Staff`, `Measure`, `Note`, `Rest`, `GraceNote`, `Ornament`, `Tuplet`, `Tie`, `Slur`, `Wedge`, `Dynamic`, `Clef`, `KeySignature`, `TimeSignature`, `Tempo`, `Barline`, `Repeat`, `Metadata`).
- `domain/music` : contrats `ScoreRepository`, `ScoreRenderer`, `MusicXmlImporter`, `MusicXmlExporter`, `MidiRenderer`, `PlaybackController`, `ScorePersistence`, `FileManager`, `SettingsRepository`.
- `data/repository` : implémentations de persistance, initialement en mémoire pour garder la fondation compilable.
- `rendering` : surface Android `View` dédiée au rendu de partition; le moteur de notation complet sera ajouté plus tard.
- `ui` : navigation et écrans Home/Create/Open/Edit/Staff/Settings/Metadata/Share.
- `ui/state` : états de chargement/erreur/vide, brouillon transactionnel et protection des modifications.

## navigation

`Home → CreateScore → EditScore → StaffConfiguration / MetadataEditor / ShareScore`

`Home → OpenScores → EditScore`

Les destinations sont identifiées par des routes stables et le domaine n'a aucune dépendance vers Compose.

## build

```bash
gradle assembleDebug
gradle testDebugUnitTest
gradle lint
```

APK attendu : `app/build/outputs/apk/debug/app-debug.apk`.
