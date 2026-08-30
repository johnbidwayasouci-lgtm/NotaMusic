# build status

Date: 2026-08-30

## état

La branche `android-foundation` contient maintenant une première application Android native navigable de bout en bout.

- Kotlin + Jetpack Compose : configurés.
- Package indépendant : `com.notationstudio.app`.
- Modèle musical extensible : créé et enrichi.
- Contrats de domaine : créés.
- Navigation : Home, CreateScore, OpenScores, EditScore, StaffConfiguration, Settings, MetadataEditor et ShareScore.
- Création en trois étapes : métadonnées, paramètres musicaux, instruments.
- Catalogue interne indépendant : claviers, cordes, bois, cuivres, percussions et voix.
- Configuration des portées : ajout, suppression, ordre et application.
- Open : état vide, liste, ouverture et confirmation de suppression.
- Settings : préférences persistantes via SharedPreferences.
- Éditeur : voix, lecture/arrêt d'interface, groupes d'outils repliables, zoom et surface Canvas.
- ScoreCanvasView : plusieurs portées, clés, signatures, mesures, notes, zoom et défilement tactile.
- Métadonnées : édition et sauvegarde.
- Partage : flux Android Sharesheet et points d'entrée MusicXML/MIDI/image.
- Prévention de perte de données : état `dirty` et confirmation de sortie depuis l'éditeur via le flux applicatif.
- CI GitHub Actions : configurée pour Java 17, Gradle 8.9, tests et APK debug.

## build local

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

## apk

Chemin attendu après compilation :

`app/build/outputs/apk/debug/app-debug.apk`

## vérification effectuée

Une compilation Android réelle ne peut pas être exécutée dans l'environnement de cette session : le SDK Android et le wrapper Gradle exécutable ne sont pas disponibles ici. Je ne marque donc pas artificiellement le build comme réussi.

Le dépôt contient le workflow CI destiné à effectuer cette validation sur GitHub Actions.

Audit statique de cette étape :

- boutons et destinations principales reliés : oui ;
- états vides : oui ;
- suppression avec confirmation : oui ;
- préférences persistantes : oui ;
- création → éditeur : oui ;
- éditeur → métadonnées/portées/partage : oui ;
- retour de navigation : oui pour les contrôles de navigation et le back stack Android ;
- surface de partition interactive : oui, rendu simplifié ;
- moteur musical complet : volontairement non implémenté ;
- export MusicXML/MIDI réel : volontairement non implémenté à ce stade.

## prochaine étape

Valider le build CI, installer l'APK sur un appareil/émulateur Android, puis corriger les écarts visuels et comportementaux observés avant d'implémenter le moteur musical complet.