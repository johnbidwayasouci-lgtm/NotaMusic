# Android release

## Pipeline unique

NotaMusic utilise `.github/workflows/ci.yml` comme pipeline Android unique.

- push sur `main` : tests, lint et APK debug
- pull request vers `main` : tests, lint et APK debug
- tag `v*` : tests, lint, APK debug, APK release signé, AAB release signé, vérification cryptographique, artefacts et GitHub Release
- `workflow_dispatch` : exécution manuelle du même pipeline ; une publication officielle nécessite toujours un tag `v*`

Les anciens workflows Android dupliqués ou utilisant un secret différent ont été supprimés afin d'éviter plusieurs pipelines concurrents.

## Signature

Le build de release utilise exclusivement les secrets GitHub Actions :

- `RELEASE_KEYSTORE_BASE64`
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

Aucune valeur de secret ne doit être commitée ou imprimée dans les logs.

Le keystore est décodé uniquement dans `$RUNNER_TEMP`, utilisé pendant le build, puis supprimé.

Le build release définit `RELEASE_SIGNING_REQUIRED=true`. Si un secret ou paramètre de signature manque, Gradle échoue au lieu de produire silencieusement un artefact de production non signé.

## Validation de signature

L'APK est vérifié avec `apksigner verify --verbose` et son certificat est comparé au certificat du keystore fourni.

L'AAB est vérifié avec `jarsigner -verify` et son certificat est comparé au certificat du même keystore.

L'identité Android est également contrôlée :

- applicationId : `org.notamusic.app`
- versionName : `0.2.0`
- versionCode : `2`

## Artefacts

Sur un tag `v0.2.0`, la pipeline publie :

- `NotaMusic-v0.2.0-release.apk` — installation directe sur Android
- `NotaMusic-v0.2.0-release.aab` — bundle destiné notamment à Google Play

Les deux fichiers sont également disponibles comme artefacts du workflow.

## Déclencher une release

Après validation de `main`, créer un tag versionné :

```bash
git tag v0.2.0
git push origin v0.2.0
```

Le tag doit correspondre à la version réellement configurée dans Gradle. Ne changez pas la version uniquement pour contourner une erreur CI.

## Sécurité

Ne jamais committer :

- `*.jks`
- `*.keystore`
- `*.p12`
- mots de passe de signature
- valeurs Base64 du keystore

Le keystore release existant reste exclusivement géré hors du dépôt et via les secrets GitHub Actions.
