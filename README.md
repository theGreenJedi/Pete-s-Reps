# Pete's Reps

**Pete's Reps** is an Android-first, local-first daily training app built around one simple idea: a workout program should keep learning from what you actually do instead of ending after eight weeks or eventually sending you back to beginner prescriptions.

Pete's Reps generates varied bodyweight sessions, records actual performance, and adapts future work from that history. Workouts are hard-capped at **25 minutes**. A pull-up bar is the only required piece of exercise equipment.

The project is intentionally small, owner-controlled, and usable without an account, subscription, cloud service, analytics platform, or permanent network connection.

## What makes Pete's Reps different

- **25 minutes is a hard ceiling.** Progress is never achieved by turning a short daily session into a two-hour workout.
- **The app remembers.** If Day 1 prescribes three push-ups, Day 300 should reflect the work completed during Days 1-299 rather than starting over at three push-ups.
- **Progression has multiple dimensions.** Reps, sets, movement difficulty, leverage, range of motion, tempo, pauses, unilateral work, rest, and density can all progress.
- **Variety is structured, not random.** Anchor movements provide measurable progression while rotating accessory, mobility, movement, and conditioning work prevents the same handful of exercises from repeating forever.
- **Eight-week / 56-day cycles organize training; they do not end it.** Day 57 begins the next cycle. Day 300, Day 600, and beyond remain part of the same longitudinal training history.
- **Broad physical capability matters.** Strength, pulling, pushing, legs, trunk strength, mobility, conditioning, crawling, ground movement, balance, and martial movement all belong in the library.
- **Training inspiration is broad.** Strength Side-style mobility and bodyweight work, U.S. Marine Corps physical-training traditions, calisthenics, and martial-arts movement inform the philosophy. Pete's Reps uses its own programming, descriptions, exercise library, and progression logic.
- **Exercise demonstrations are supported.** Exercises can carry local video, animation, image-sequence, or movement-cue assets, but demonstrations are not required for the training engine to function.
- **Your data stays local.** Training history is stored on the device in SQLite.

See [docs/CANON.md](docs/CANON.md) for the project rules and design constraints.

## Current status

Pete's Reps is an **early experimental Android build**. The first milestone is a dependable adaptive training engine rather than a giant fitness platform.

The current implementation includes:

- adaptive daily workout generation
- a hard 25-minute workout budget
- persistent local workout history
- 56-day cycles with indefinite rollover
- a varied starter exercise library
- performance-based progression
- exercise instructions / movement cues
- unit tests for progression, cycle rollover, and the 25-minute ceiling
- GitHub Actions CI that tests the project and produces a debug APK

Expect the interface, exercise library, progression rules, and data model to evolve.

## Android support

Pete's Reps is being developed **Android-first**.

Current build configuration:

- Kotlin / Jetpack Compose
- Android Gradle Plugin 9.3.0
- Kotlin / Compose compiler plugin 2.4.10
- Compose BOM 2026.08.00
- `compileSdk 37` — Android 17
- `targetSdk 36`
- `minSdk 26`
- JDK 17 / Gradle 9.5.0
- SQLite for durable local history

Android 17 is API level 37. Pete's Reps currently compiles with the Android 17 SDK while targeting API 36, so Android 17 can run the app while we continue validating Android-17-specific behavior before moving the target SDK to 37.

# Installing Pete's Reps on Android 17

Pete's Reps is not currently distributed through Google Play. Installation is therefore a **sideload** from a Pete's Reps APK built by this repository.

## Easiest method — install a GitHub-built APK

1. On the Android 17 device, open this GitHub repository.
2. Open the **Actions** tab.
3. Open the most recent successful **Android CI** run — look for the green check mark.
4. Scroll to **Artifacts** and download `petes-reps-debug-apk`.
5. The artifact downloads as a ZIP file. Open or extract it with Files or another file manager.
6. Inside the ZIP, locate `app-debug.apk`.
7. Tap `app-debug.apk`.
8. Android may tell you that the browser or file manager is not currently allowed to install unknown apps. Allow installs **for that source only** when prompted. If Android does not offer the prompt directly, search Android Settings for **Install unknown apps**, select the app you are using to open the APK (for example Chrome or Files), and allow that source.
9. Return to the APK and tap **Install**.
10. When installation completes, open **Pete's Reps** from the launcher.

Android / Google Play Protect may scan or warn about a sideloaded APK. That is expected for software installed outside Google Play. Only install an APK that you intentionally obtained from this repository or a Pete's Reps release that you trust.

## Android 17: "unverified developer" message

Newer Android security controls can separately block apps from an **unverified developer**. This is different from the ordinary "install unknown apps" permission above and is being rolled out progressively.

If Android specifically says the app cannot be installed because the developer is unverified, and you intentionally want to install this personal build:

1. Enable Android **Developer options** if they are not already enabled.
2. Open **Settings → System → Developer options**.
3. Turn on **Allow apps from unverified developers**.
4. Follow Android's security flow. Google currently documents a **24-hour security delay** before this bypass becomes available.
5. After the delay and confirmation flow, retry the APK and choose **Install anyway** when Android presents that option.

Do **not** disable this protection merely because a website, caller, text message, or stranger tells you to install an APK. This exception is intended for a build you personally know and trust.

> Note: Android Advanced Protection can block installation from unknown sources entirely. If Advanced Protection is enabled, normal sideloading may not be available.

## Installing with ADB instead

Developers can install the APK from a computer with Android Platform Tools:

1. Enable **Developer options** on the Android device.
2. Enable **USB debugging**.
3. Connect the phone to the computer and approve the debugging prompt on the phone.
4. Download and extract the Pete's Reps APK artifact.
5. From the directory containing the APK, run:

```bash
adb install app-debug.apk
```

For an update over an existing build, normally use:

```bash
adb install -r app-debug.apk
```

### Early-build signing warning

Current CI artifacts are **debug builds**. Different debug builds can occasionally be signed with a different debug key, in which case Android will refuse to install the new APK over the existing copy. If Android reports a signature / package conflict, uninstalling the old copy will allow the new one to install — **but uninstalling also removes the app's local training database**.

Do not rely on uninstall/reinstall as an update strategy once you have meaningful training history. Stable release signing plus export/restore of training history are therefore important roadmap items before Pete's Reps becomes a long-term daily driver.

## Building the APK yourself

The repository CI installs the Android SDK and Gradle 9.5.0, runs the unit tests, and builds a debug APK.

Locally, open the project in a current Android Studio release that supports the Android 17 SDK, or build with Gradle 9.5.0 and JDK 17:

```bash
gradle :app:testDebugUnitTest :app:assembleDebug
```

The APK is written to:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Updating Pete's Reps

For now, updates are manual:

1. Obtain a newer Pete's Reps APK from a successful GitHub build or future GitHub Release.
2. Install it over the existing app.
3. Android should retain the local training database when the package and signing key match.

A future release process should provide consistently signed APKs so normal upgrades preserve training history reliably.

## Safety

Pete's Reps is training software, not medical care. Exercise within your ability, use a securely installed pull-up bar, and stop a movement that causes sharp pain, dizziness, or other concerning symptoms.

## Privacy

Pete's Reps is designed to work locally. The core app does not require an account, analytics service, subscription, or cloud connection. Workout history lives on the device.

## License

MIT. See [LICENSE](LICENSE).
