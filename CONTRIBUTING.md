# Contributing to Shizuku Manager

Thank you for considering a contribution to Shizuku Manager! We welcome bug reports,
feature suggestions, documentation improvements, and code contributions.

## Development Environment

### Required Tools

- Android Studio (latest stable)
- Android SDK API 36 (compileSdk), minSdk 24
- JDK 11+
- Git
- A physical device or emulator with Shizuku installed (for testing)

### Project Setup

```bash
git clone https://github.com/your-username/shizuku-manager.git
cd shizuku-manager
```

Open the `shizuku-manager/` directory in Android Studio and sync Gradle.

## Build Process

### Debug Build

```bash
./gradlew assembleDebug
```

### Release Build

Release builds require a private signing configuration. See [docs/BUILDING.md](./docs/BUILDING.md).

```bash
./gradlew assembleRelease
```

> **Note**: `signing.properties` and `release-key.jks` are never committed. Create
> `signing.properties` from `signing.properties.example` with your own values.

## Running Tests

### Unit Tests

```bash
./gradlew test
```

### Instrumented Tests

Requires a connected device or emulator:

```bash
./gradlew connectedAndroidTest
```

### Lint

```bash
./gradlew lintDebug
```

## Coding Conventions

### Language

- Source code: Java 11
- Build scripts: Kotlin DSL (Gradle)
- Comments in Java: Vietnamese without diacritics (tiếng Việt không dấu)
- String resources: English (default) + Vietnamese (`values-vi`)

### Package Structure

Follow the existing package-per-layer organization:

```
com.shizukumanager.app.database/   Room entities, DAOs
com.shizukumanager.app.di/         Manual DI container
com.shizukumanager.app.model/      Data models, enums
com.shizukumanager.app.pkg/        Package scanning & operations
com.shizukumanager.app.repository/ Repository layer
com.shizukumanager.app.shizuku/    Shizuku integration
com.shizukumanager.app.ui/         Activities, fragments, adapters
com.shizukumanager.app.util/       Utilities
com.shizukumanager.app.viewmodel/  ViewModels
```

### Naming

- Class names: `PascalCase` (e.g. `AppRepository`, `ShizukuConnectionManager`)
- Method names: `camelCase` (e.g. `refreshPackages`, `executeShellCommand`)
- Variable names: `camelCase` (e.g. `appContainer`, `viewModel`)
- Constants: `UPPER_SNAKE_CASE` (e.g. `SHIZUKU_PACKAGE_NAME`)
- Package names: lowercase with dots (e.g. `com.shizukumanager.app.repository`)
- Avoid generic names like `DataManager`, `CommonUtils`, `GenericProcessor`

### Comments

- Comments in Java must be in Vietnamese without diacritics.
- Comment the WHY, not the WHAT.
- Do not add comments that merely restate the code.
- Do not modify string literals (user-visible text).

### Do Not

- Do not introduce Dagger/Hilt or other DI frameworks.
- Do not add unnecessary interfaces or wrapper classes.
- Do not include private credentials, keystore files, or signing passwords.
- Do not change the package namespace without team discussion.

## Pull Request Process

1. Fork the repository and create a new branch from `main`.
2. Make your changes and commit with a clear message.
3. Run tests (`./gradlew test`) and lint (`./gradlew lintDebug`) before pushing.
4. Push your branch and open a Pull Request against `main`.
5. A maintainer will review your PR. Address any feedback before merging.

### Commit Message Convention

Use conventional commit prefixes:

- `feat:` — new feature
- `fix:` — bug fix
- `refactor:` — code refactoring
- `docs:` — documentation changes
- `test:` — adding or updating tests
- `chore:` — build/tooling changes

Example: `feat: add suspend/unsuspend support for Android 10+`

## Security

Do not disclose security vulnerabilities in public issues. See [SECURITY.md](./SECURITY.md)
for responsible disclosure instructions.

## Code of Conduct

Please read and follow our [Code of Conduct](./CODE_OF_CONDUCT.md).

## License

By contributing, you agree that your contributions will be licensed under the MIT License.
