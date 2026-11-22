# Changelog

All notable changes to Shizuku Manager are documented in this file.

## [1.0.0] - 2026-11-22

### Added

- Initial public release
- Enable / Disable / Suspend / Unsuspend / Freeze / Unfreeze / Force Stop / Uninstall
- App Profiles with JSON export/import and apply/restore
- Operation history with success/failure filtering
- Shizuku integration (non-root package management)
- LiveData-based UI with Manual DI + Repository + MVVM
- Localization: English and Vietnamese
- Material Design 3 with glassmorphism background
- Unit tests (JUnit, Robolectric) and instrumented tests (Espresso)

### Security

- Removed private keystore (`release-key.jks`) from repository
- Removed hardcoded signing passwords from `gradle.properties`
- Added `signing.properties.example` for user-provided release signing
- Added `.gitignore` rules for keystore, signing properties, and IDE metadata

### Changed

- Renamed Java package from `com.example` to `com.shizukumanager.app`
- All Java comments standardized to Vietnamese without diacritics
