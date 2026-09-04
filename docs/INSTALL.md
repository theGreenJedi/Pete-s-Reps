# Download, Install & Power Up Pete's Reps

Pete's Reps is currently an early Android build distributed from this GitHub repository. It does not require a Pete's Reps account, subscription, analytics service, or cloud connection to run.

## Fastest path: download the GitHub-built APK

1. Open the Pete's Reps repository on GitHub.
2. Open **Actions**.
3. Choose the newest successful **Android CI** run (green check mark) on `main`.
4. In **Artifacts**, download **`petes-reps-debug-apk`**.
5. Extract the downloaded ZIP.
6. Open **`app-debug.apk`** on the Android device.
7. If Android asks whether the browser or file manager may install apps from this source, allow that source only if you intentionally downloaded this Pete's Reps build.
8. Confirm **Install**.
9. Open **Pete's Reps** from the launcher.

The GitHub Actions artifact is the current repository download path. It is a debug build and is intended for development/testing rather than polished public distribution.

## Powering up for the first time

No onboarding questionnaire or fitness test is required in v0.

On launch:

1. Pete's Reps generates the next session from the local training profile.
2. The first screen shows the complete session overview.
3. Review the movements and equipment before starting.
4. Tap **Start session**.
5. The app switches to one movement at a time.
6. Record the smallest objective result requested by the current build (reps or seconds).
7. Use **Next** to continue. **View full session** returns to the overview at any time.
8. On the final movement, tap **Complete session**.
9. Pete's Reps stores the results locally and uses them when generating the next session.

The engine's progression, readiness, capability, and goal-selection logic is intentionally invisible during normal use.

## Core equipment

Pete's Reps currently assumes access to:

- a securely installed pull-up bar
- a kettlebell
- a medicine ball

Conventional dumbbells, barbells, plates, or other weights may be useful later but are not required by the core design.

## Build the APK yourself

The repository CI runs the unit tests and assembles the debug APK.

Local requirements currently include:

- JDK 17
- Gradle 9.5.0
- Android SDK matching the repository build configuration

From the repository root:

```bash
gradle :app:testDebugUnitTest :app:assembleDebug
```

The APK is produced at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

You can install it from a connected development computer with Android Platform Tools:

```bash
adb install app-debug.apk
```

For an update signed with the same key:

```bash
adb install -r app-debug.apk
```

## Updating without losing training history

Training history is local to the app installation.

Current CI artifacts are debug builds. Debug signing can differ between build environments. If Android reports a signing/package conflict, uninstalling the old app may allow a fresh install, but uninstalling also removes the local Pete's Reps database.

Therefore:

- do not treat uninstall/reinstall as a normal update strategy once the device contains meaningful training history
- stable release signing is a required milestone before Pete's Reps becomes a dependable long-term daily-driver install
- export/restore of training history is also a planned durability feature

## GitHub Releases

The intended public-download end state is a consistently signed APK attached to a GitHub Release, with the README linking directly to the latest release. Until stable signing is configured, the repository's successful Android CI artifact remains the authoritative downloadable build.

## Privacy / network behavior

Pete's Reps is local-first. The core training flow does not require an account or permanent network connection. Training history is stored on the device.

## Training note

Use equipment that is securely installed and appropriate for the prescribed movement. Stop a movement that produces sharp pain, dizziness, or another concerning symptom. Pete's Reps is training software, not medical care.
