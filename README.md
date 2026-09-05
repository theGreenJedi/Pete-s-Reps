# Pete's Reps

**Pete's Reps** is an Android-first, local-first physical-training app built around one rule: **capability first**.

Open the app, see today's work, train for no more than **25 minutes total**, record what actually happened, and leave. The engine remembers the longitudinal history and quietly chooses the next appropriate work.

No account. No subscription. No social feed. No visible progression ladders. No benchmark-test days required.

## Download & power up

Pete's Reps is currently an early experimental Android build.

**Fastest current install path:**

1. Open this repository's **Actions** tab.
2. Open the newest successful **Android CI** run on `main`.
3. Download the **`petes-reps-debug-apk`** artifact.
4. Extract the ZIP and install **`app-debug.apk`** on the Android device.
5. Open Pete's Reps, review the complete session, tap **Start session**, and train.

Full step-by-step instructions, ADB installation, updating cautions, and first-run behavior are in **[docs/INSTALL.md](docs/INSTALL.md)**.

> The current downloadable build is still a debug APK. The repository now contains the stable signing/GitHub Release pipeline, but the durable private signing key must be provisioned into GitHub Actions secrets before the first update-safe release can be published.

## Training-history durability

Pete's Reps 0.3.0 adds **full training-history export and restore** from inside the app.

The backup includes completed sessions, objective performance history, and the hidden progression/readiness state needed to continue the engine rather than merely preserving a workout log.

Use **Export training backup** before any uninstall, phone replacement, debug-signing transition, or other operation that could remove the app's private local database. Restore validates the selected file first and then replaces local history atomically in one SQLite transaction.

See **[docs/BACKUP.md](docs/BACKUP.md)** for the backup contract and recovery procedure.

## Release distribution status

The production release path is documented in **[docs/RELEASE.md](docs/RELEASE.md)** and implemented by **`.github/workflows/release.yml`**.

Once the signing secrets are installed, a matching `v*` tag will:

- verify that the tag matches the Android `versionName`
- run tests and build the release APK
- sign it with the durable Pete's Reps release key
- verify the APK signature with Android `apksigner`
- generate a SHA-256 checksum
- publish the signed APK and checksum to GitHub Releases

The private keystore is intentionally excluded from source control.

## The training contract

- **Capability first.** Working strength, longevity, mobility/flexibility, conditioning, body control, balance, coordination, grip, power, locomotion, and durability support that objective.
- **6 training days per week.** The app targets six sessions without turning the seventh day into a hidden workout.
- **25 minutes means 25 minutes.** Preparation, training, conditioning, mobility, stretching, transitions, and logging all fit inside the cap.
- **Working strength over gym numbers.** Grip, hanging, pulling, overhead strength, carries, awkward-load control, body control, and strength through useful ranges matter more than chasing isolated maxes.
- **Progress is inferred from ordinary training.** The workout is the evidence; there are no required benchmark days.
- **Recovery is inferred from results.** Pete's Reps adapts from demonstrated performance rather than mandatory sleep/soreness/readiness questionnaires.
- **Failure is data, not the goal.** The program does not routinely chase muscular failure.
- **Progression is hidden.** Handstand, grip, pull-up, pistol, L-sit, carry, rope-climb, and other progressions exist inside the engine; the user gets the next appropriate movement.
- **Stretching is training.** Mobility and flexibility are first-class parts of the program.
- **Complex engine, simple interface.** Internal scores and selection reasons remain invisible plumbing.

See **[docs/CANON.md](docs/CANON.md)** for the non-negotiable project rules.

## Current v0 engine

The current implementation now includes:

- rolling exposure-based session generation instead of a fixed seven-day body-part split
- four blocks totaling 23 planned minutes, leaving transition/logging slack under the 25-minute ceiling
- a mobility/stretching block in every v0 session
- performance-based challenge progression
- inferred readiness from material performance degradation
- hidden capability and long-term-goal metadata
- structured exercise variety with recent-repeat avoidance
- pull-up bar, kettlebell, and medicine-ball movement support
- handstand, grip, carry, L-sit, pistol, rope-climb-support, crawling, mobility, and conditioning movements
- full-session overview before training
- focused one-movement-at-a-time execution with the overview always available
- persistent local workout history
- full local training-history export/restore with a versioned app-owned backup format
- unit tests for the time ceiling, six-session rhythm, mobility exposure, progression, readiness adjustment, capability links, and backup codec
- GitHub Actions CI that tests the app and builds a downloadable debug APK
- a tag-driven signed-release pipeline awaiting durable signing-secret provisioning

The implementation-facing design is documented in **[docs/V0_ENGINE_SPEC.md](docs/V0_ENGINE_SPEC.md)**.

## Core equipment

Pete's Reps assumes a deliberately small kit:

- pull-up bar
- kettlebell
- medicine ball

Conventional weights can be used opportunistically later, but they are not required and are not the organizing goal of the program.

## Android / build

Current project configuration:

- Kotlin / Jetpack Compose
- `compileSdk 37`
- `targetSdk 36`
- `minSdk 26`
- JDK 17
- Gradle 9.5.0 in CI
- local SQLite training history

Build and test from the repository root:

```bash
gradle :app:testDebugUnitTest :app:assembleDebug
```

APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

The normal CI also smoke-tests `assembleRelease` without signing material; unsigned release outputs from that smoke test are never published.

## Architecture

The project intentionally remains small while the training engine is being proven:

```text
app/src/main/java/org/petesreps/
├── data/
│   ├── ExerciseCatalog.kt
│   ├── TrainingBackup.kt
│   └── TrainingDatabase.kt
├── engine/
│   ├── CapabilityGraph.kt
│   └── WorkoutEngine.kt
├── model/
│   └── Models.kt
└── ui/
    └── PetesRepsScreen.kt
```

A future refactor may move the engine/data/model layers into separate Gradle modules and migrate persistence to Room. Those are engineering improvements, not reasons to delay validating the adaptive training behavior.

## Privacy

Pete's Reps is designed to work locally. The core training flow does not require an account, analytics service, advertising service, subscription, or permanent network connection. Workout history lives on the device.

## Safety

Pete's Reps is training software, not medical care. Use securely installed equipment and appropriate movement space. Stop a movement that causes sharp pain, dizziness, or another concerning symptom.

## License

MIT. See [LICENSE](LICENSE).
