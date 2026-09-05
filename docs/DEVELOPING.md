# Developing Pete's Reps

This page is for developers building Pete's Reps from source. Normal Android installation instructions are in **[INSTALL.md](INSTALL.md)**.

## Current Android configuration

- Kotlin / Jetpack Compose
- `compileSdk 37`
- `targetSdk 36`
- `minSdk 26`
- JDK 17
- Gradle 9.5.0 in CI
- local SQLite training history

## Build and test

From the repository root:

```bash
gradle :app:testDebugUnitTest :app:assembleDebugAndroidTest :app:assembleDebug
```

The debug APK is produced at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Normal CI also smoke-tests `assembleRelease` without signing material. Unsigned release outputs from that smoke test are never published.

## Install with ADB

With Android Platform Tools and a connected development device:

```bash
adb install app-debug.apk
```

For an update signed with the same key:

```bash
adb install -r app-debug.apk
```

## Debug-signing caution

Debug signing can differ between build environments. If Android reports a signing/package conflict, uninstalling the old app may permit a fresh install, but uninstalling also removes the app's private local database.

Before any uninstall/reinstall path, export a Pete's Reps training backup from inside the app.

Do not treat debug signing as the long-term distribution path. Durable production signing and GitHub Release distribution are documented in **[RELEASE.md](RELEASE.md)**.

## CI artifact

The GitHub Actions workflow tests the app, compiles Compose instrumentation tests, assembles the debug APK, smoke-tests the release configuration, and uploads **`petes-reps-debug-apk`**.

The ordinary phone-install path uses that artifact and is documented separately in **[INSTALL.md](INSTALL.md)**.
