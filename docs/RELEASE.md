# Release Process

## Versioning

Releases follow [Semantic Versioning](https://semver.org/) (`MAJOR.MINOR.PATCH`).
Version code and version name are defined in `app/build.gradle.kts`:

```kotlin
versionCode = 1
versionName = "1.0.0"
```

## Release Signing

Release APKs must be signed with a release keystore. Credentials are configured via
`signing.properties` (created from `signing.properties.example`) and are never committed.

See [BUILDING.md](./BUILDING.md) for detailed steps.

## Creating a Release

1. Update `versionCode` and `versionName` in `app/build.gradle.kts`.
2. Create `signing.properties` with your release keystore values.
3. Run `./gradlew assembleRelease`.
4. Test the signed APK on a physical device.
5. Create a Git tag: `git tag v1.0.0 && git push origin v1.0.0`.
6. Create a GitHub Release from the tag.
7. Attach the release APK to the GitHub Release.

## What Is NOT Automated

- No GitHub Actions workflow for Release APK signing is included, because signing
  credentials must remain private. The workflow in `.github/workflows/` only builds
  debug APKs and runs tests for pull requests.
- No automatic Play Store publishing.

## Rollback

If a release introduces a critical bug:
1. Create a hotfix branch from the previous tag.
2. Fix the issue, bump version code/name.
3. Sign and release as a new version (e.g. `1.0.1`).
