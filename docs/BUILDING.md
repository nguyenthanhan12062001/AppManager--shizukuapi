# Building Shizuku Manager

## Prerequisites

- Android Studio (latest stable release)
- Android SDK API 36 (compileSdk)
- Minimum SDK: API 24
- JDK 11+
- Git
- Shizuku app installed on device/emulator (for full functionality, optional for build)

## Clone the Repository

```bash
git clone https://github.com/your-username/shizuku-manager.git
cd shizuku-manager
```

## Open in Android Studio

1. Launch Android Studio.
2. Select **File > Open**.
3. Choose the `shizuku-manager/` directory.
4. Allow Android Studio to sync Gradle files.
5. Wait for the initial Gradle sync to complete.

## Debug Build

No signing configuration is required for debug builds.

```bash
./gradlew assembleDebug
```

Output APK: `app/build/outputs/apk/debug/app-debug.apk`

Install on a connected device:

```bash
./gradlew installDebug
```

## Release Build

Release builds require a signing configuration. Private keystore credentials are
**not** included in the repository.

### Step 1: Create signing.properties

Copy the example file and fill in your own values:

```bash
cp signing.properties.example signing.properties
```

Edit `signing.properties`:

```
storeFile=/path/to/your/release-key.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=YOUR_KEY_ALIAS
keyPassword=YOUR_KEY_PASSWORD
```

- `signing.properties` is listed in `.gitignore` and must never be committed.
- `storeFile` can be an absolute path or relative to the project root.

### Step 2: Build Release

```bash
./gradlew assembleRelease
```

Output APK: `app/build/outputs/apk/release/app-release.apk`

### Step 3: Validation

The build includes a `validateReleaseSigning` task that runs before `assembleRelease`.
If `signing.properties` is missing or incomplete, the build fails with a clear error
message:

```
Release signing configuration missing.
Create signing.properties from signing.properties.example and fill in your own keystore values.
```

or:

```
Release signing.properties is incomplete.
Missing: storePassword, keyPassword.
Edit signing.properties with your own keystore values.
```

## Running Tests

### Unit Tests (JUnit + Robolectric)

```bash
./gradlew test
```

### Instrumented Tests (Espresso)

Requires a connected device or emulator:

```bash
./gradlew connectedAndroidTest
```

### All Tests

```bash
./gradlew test connectedAndroidTest
```

## Running Lint

```bash
./gradlew lint
```

Lint report: `app/build/reports/lint-results.html`

## Clean Build Artifacts

```bash
./gradlew clean
```

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `signing.properties not found` | Copy `signing.properties.example` to `signing.properties` and fill in values |
| `Release signing.properties is incomplete` | Fill in all four required fields in `signing.properties` |
| Gradle sync fails | Ensure Android SDK API 36 is installed via SDK Manager |
| Shizuku not detected | Install Shizuku app from https://shizuku.rikka.app/ and enable developer options |
