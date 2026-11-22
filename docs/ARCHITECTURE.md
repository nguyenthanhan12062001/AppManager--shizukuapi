# Architecture Overview

Shizuku Manager follows a simple, pragmatic **Manual DI + Repository + MVVM** architecture.
No heavyweight DI framework (Dagger/Hilt) is used. No Clean Architecture layers are forced
on features that do not need them.

## Design Principles

- **Simplicity over complexity**: No unnecessary abstractions, no interfaces for the sake
  of interfaces, no wrapper classes without value.
- **Readability over cleverness**: Meaningful names, specific method responsibilities,
  shallow nesting, no duplicated logic.
- **Consistency**: One package-per-layer organization, consistent naming across modules.
- **Engineering authenticity**: Keep only technical decisions with real reasons. Remove
  dead code, unused imports, and defensive code that does not add safety.

## Module Boundaries

```
app/src/main/java/com/shizukumanager.app/
├── database/          Room entities, DAOs, AppDatabase (local cache only)
├── di/                Manual DI container (AppContainer)
├── model/             Data models, enums, result objects (UI-independent)
├── pkg/               Package scanning & system operations
├── repository/        Repository layer (single source of truth per domain)
├── shizuku/           Shizuku connection manager + shell service AIDL
├── ui/                Activities, fragments, adapters, dialogs, custom views
├── util/              Utilities (icons, preferences, label mapping, dates)
└── viewmodel/         MVVM ViewModels + ViewModelFactory
```

### Dependency Direction

```
UI (Fragments/Adapters)
    v
ViewModel
    v
Repository
    v
Data Sources (PackageManager, Shizuku Binder, Room DAO)
```

Dependencies flow downward. UI never talks directly to PackageManager or Shizuku —
all access goes through Repository → Data Source.

## Layer Responsibilities

### database/
Room entities and DAOs. Used as a **local cache** for app metadata, profiles, saved states,
and operation history. Not a replacement for PackageManager — PackageManager is the
single source of truth for real-time app state.

### di/
`AppContainer` is a manual DI container. It instantiates and holds singletons:
executors, preferences, safety guard, Shizuku manager, repositories, view model factory.
No annotation processing. No code generation.

### model/
Plain Java data classes and enums. UI-independent. Examples:
- `AppInfoModel` — queried directly from PackageManager
- `AppState` — ENABLED, DISABLED, SUSPENDED, DISABLED_AND_SUSPENDED
- `OperationResult` — result of a single package operation
- `ErrorType` — classified error categories
- `BulkOperationSummary` — aggregate result of batch operations

### pkg/
System-level package operations.
- `SystemPackageScanner` — reads PackageManager (installed packages, enabled state,
  suspended state, system flag)
- `PackageOperationsManager` — executes enable/disable/suspend/freeze/force-stop/
  uninstall via Shizuku shell, verifies result by re-querying PackageManager
- `PackageEventReceiver` — listens for package change broadcasts
- `PackageSafetyGuard` — blocks operations on critical system packages and self

### repository/
Business logic and data coordination.
- `AppRepository` — scans packages, executes single/bulk operations, syncs Room cache,
  exposes LiveData for UI observation
- `ProfileRepository` — CRUD for app profiles, JSON export/import, apply/restore states
- `HistoryRepository` — read/clear operation history from Room

### shizuku/
Shizuku integration layer.
- `ShizukuConnectionManager` — binds to Shizuku UserService, manages Binder lifecycle,
  exposes connection status as LiveData
- `ShizukuShellService` — AIDL stub running inside Shizuku process, executes `pm`/`am`/`cmd`
  commands with a 15-second timeout
- `CommandExecutionResult` — structured result (exit code, stdout, stderr, binder lost)

### ui/
Android Views with Material Design 3. Fragments observe ViewModels via LiveData.
ViewBinding is used throughout. Navigation via Navigation Component.

### util/
- `AppIconLoader` — async icon loading with LruCache and WeakReference to ImageView
- `AppPreferencesManager` — SharedPreferences wrapper, language tag persistence
- `LocalizedLabelMapper` — maps ErrorType/OperationType/AppState to localized strings
- `DateTimeFormatterUtil` — date/time formatting helpers

### viewmodel/
One ViewModel per screen. Created via `AppViewModelFactory` (manual DI).
ViewModels hold repository references and expose LiveData + event LiveData patterns.

## State Management

- UI state is observed via `LiveData` from repositories.
- One-shot events (operation results, navigation) use `MutableLiveData` with
  `consume*()` pattern to avoid re-delivery on configuration changes.
- ExecutorService instances (IO pool of 2 threads, icon pool of 3 threads) are
  managed by `AppContainer` and shut down in `shutdown()`.

## Concurrency

- All PackageManager and Shizuku calls run on background executor threads.
- Results are posted back to the main thread via `Handler(Looper.getMainLooper())`.
- Room queries are observed via LiveData (Room handles threading internally).
- `ShizukuConnectionManager` uses `CountDownLatch` with 4-second timeout for
  service binding, with volatile fields for thread-safe Binder access.

## Lifecycle & Resource Cleanup

- `AppContainer.shutdown()` — shuts down executors and cleans up Shizuku bindings.
- Fragments set `binding = null` in `onDestroyView()` to avoid memory leaks.
- `AppIconLoader` uses `WeakReference<ImageView>` to avoid holding dead views.
- Broadcast receivers are registered/unregistered in `onResume()`/`onDestroy()`.

## What Is NOT in the Architecture

- No Dagger/Hilt (Manual DI is sufficient for this app's size)
- No Clean Architecture use-case/interactor layer (Repository layer covers needs)
- No Compose (Views + Material Design 3)
- No Kotlin coroutines (Java + ExecutorService + Handler)
- No Retrofit/OkHttp (no network calls in this app)
