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

> The current repository artifact is a debug APK. Stable release signing and direct GitHub Release downloads are required before this becomes a dependable long-term public distribution channel.

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
- unit tests for the time ceiling, six-session rhythm, mobility exposure, progression, readiness adjustment, and capability links
- GitHub Actions CI that tests the app and builds a downloadable debug APK

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

## Architecture

The project intentionally remains small while the training engine is being proven:

```text
app/src/main/java/org/petesreps/
├── data/
│   ├── ExerciseCatalog.kt
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
