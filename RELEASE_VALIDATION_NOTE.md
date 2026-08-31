# Android release validation

The release path is validated only by a tag-triggered GitHub Actions run. It must pass unit tests, lint, release APK/AAB builds, cryptographic signature verification, artifact upload, and GitHub Release publication before being considered successful.

Release signing uses the existing GitHub Actions secrets and existing release keystore. No keystore or signing secret is stored in the repository.
