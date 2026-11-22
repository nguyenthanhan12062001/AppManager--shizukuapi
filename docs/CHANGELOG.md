Changelog

All notable changes to Shizuku Manager are documented here.

[1.0.0] - 2026-11-22

First public release of Shizuku Manager.

Added

- Manage installed applications through Shizuku without requiring root access.
- Support for enabling, disabling, suspending, unsuspending, freezing, unfreezing, force stopping, and uninstalling applications.
- App Profiles for saving and managing application states.
- Import and export profiles using JSON files.
- Operation history with success and failure status.
- Detailed error information for failed operations.
- Application filtering by enabled, disabled, and suspended states.
- Shizuku connection status monitoring.
- English and Vietnamese language support.
- Material Design 3 interface with optional gradient background effects.

Improved

- More consistent application state updates after package operations.
- Better handling of Shizuku connection changes and service interruptions.
- Improved feedback for individual and bulk operations.
- More efficient application list updates.
- Better handling of application icons and related resources.
- Improved profile management and state restoration.

Technical

- Java-based Android application.
- Room for local data storage.
- LiveData and ViewModel for UI state management.
- Repository pattern with lightweight manual dependency injection.
- Binder IPC for Shizuku communication.

Repository

- Added project documentation and setup instructions.
- Added contribution and security guidelines.
- Added example signing configuration.
- Added GitHub issue and pull request templates.
- Added repository configuration and ignore rules.
