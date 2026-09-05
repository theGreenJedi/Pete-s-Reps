# Pete's Reps

**Pete's Reps** is an Android-first, local-first physical-training app built around one rule: **capability first**.

Open the app, see today's work, train for no more than **25 minutes total**, record what actually happened, and leave. The engine remembers the longitudinal history and quietly chooses the next appropriate work.

No account. No subscription. No social feed. No visible progression ladders. No benchmark-test days required.

## Install Pete's Reps on an Android phone

You do **not** need Android Studio, ADB, Gradle, or a computer to install the current alpha.

1. On your Android phone, sign in to GitHub and open this repository.
2. Tap **Actions** near the top of the repository page.
3. Open the newest **Android CI** run on `main` that has a green check mark.
4. Scroll to **Artifacts** and tap **`petes-reps-debug-apk`**.
5. GitHub downloads a ZIP file to the phone.
6. Open the phone's **Files** app, find the ZIP in **Downloads**, and extract it.
7. Open the extracted folder and tap **`app-debug.apk`**.
8. If Android asks for permission to install apps from that browser or file manager, open **Settings**, allow that source, then return to the installer.
9. Tap **Install**.
10. Open **Pete's Reps** from the launcher.

That is the normal alpha installation path. Full phone-first help, update cautions, and first-run guidance are in **[docs/INSTALL.md](docs/INSTALL.md)**.

Developer build instructions have been separated into **[docs/DEVELOPING.md](docs/DEVELOPING.md)** so they do not get mixed up with ordinary installation.

> The current downloadable build is still a debug APK. The repository contains the stable signing/GitHub Release pipeline, but the durable private signing key still has to be provisioned before the first update-safe release can be published.

## One-tap substitute

Pete's Reps 0.5.0 added **Swap** to the session overview and current-movement screen.

If today's movement is not workable, tap **Swap** once. Pete's Reps quietly chooses another movement using the hidden capability graph.

- no reason questionnaire
- no exposed substitution score
- intended capability stimulus is preserved
- the actual replacement movement is what gets recorded
- the 25:00 master clock never resets
- a running movement timer also does not reset
- repeated swaps avoid movements already rejected in that block during the running app session
- active replacements survive Android activity/process recreation

See **[docs/SUBSTITUTION.md](docs/SUBSTITUTION.md)** for the substitution contract.

## Time is law

Pete's Reps 0.4.0 made the 25-minute contract a runtime invariant.

- one master **25:00** clock starts with the session and never resets between movements
- backgrounding, screen sleep, rotation, and ordinary process recreation do not grant extra workout time
- each current movement has a subordinate pacing timer
- an expired movement timer says **MOVE ON**
- at **00:00** the session stops automatically and saves objective results already entered
- unentered movements at a hard stop are treated as unattempted rather than fake zero-rep failures
- the active clock, current movement, entered results, and active substitutions are checkpointed locally while training

See **[docs/TIMING.md](docs/TIMING.md)** for the timing contract.

## Training-history durability

Pete's Reps can export and restore the complete local training state.

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
- **25 minutes means 25 minutes.** Preparation, training, conditioning, mobility, stretching, transitions, and logging all fit inside the cap; the live master clock hard-stops the session at 00:00.
- **One-tap substitute.** If a movement is not workable, Swap quietly chooses another movement while preserving the intended training stimulus and remaining time.
- **Working strength over gym numbers.** Grip, hanging, pulling, overhead strength, carries, awkward-load control, body control, and strength through useful ranges matter more than chasing isolated maxes.
- **Progress is inferred from ordinary training.** The workout is the evidence; there are no required benchmark days.
- **Recovery is inferred from results.** Pete's Reps adapts from demonstrated performance rather than mandatory sleep/soreness/readiness questionnaires.
- **Failure is data, not the goal.** The program does not routinely chase muscular failure.
- **Progression is hidden.** Handstand, grip, pull-up, pistol, L-sit, carry, rope-climb, and other progressions exist inside the engine; the user gets the next appropriate movement.
- **Stretching is training.** Mobility and flexibility are first-class parts of the program.
- **Complex engine, simple interface.** Internal scores and selection reasons remain invisible plumbing.

See **[docs/CANON.md](docs/CANON.md)** for the non-negotiable project rules.

## Current v0 engine

The current implementation includes:

- rolling exposure-based session generation instead of a fixed seven-day body-part split
- four blocks totaling 23 planned minutes, leaving transition/logging slack under the 25-minute ceiling
- a live 25:00 master clock with automatic hard stop
- subordinate current-movement timers for pacing
- in-progress session checkpoint/resume state based on monotonic elapsed time
- one-tap capability-preserving movement substitution with exact active-prescription resume
- a hidden intended-stimulus field so cross-family swaps do not corrupt progression
- a mobility/stretching block in every session
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
- JVM unit tests for timing, substitutions, time ceiling, six-session rhythm, mobility exposure, progression, readiness adjustment, capability links, and backup codec
- Compose instrumentation tests for visible timer states, compiled by CI
- GitHub Actions CI that tests the app and builds a downloadable debug APK
- a tag-driven signed-release pipeline awaiting durable signing-secret provisioning

The implementation-facing design is documented in **[docs/V0_ENGINE_SPEC.md](docs/V0_ENGINE_SPEC.md)**.

## Core equipment

Pete's Reps assumes a deliberately small kit:

- pull-up bar
- kettlebell
- medicine ball

Conventional weights can be used opportunistically later, but they are not required and are not the organizing goal of the program.

## Android / development

Pete's Reps is currently Kotlin + Jetpack Compose with local SQLite history. Build/test commands, SDK versions, APK paths, and ADB instructions are in **[docs/DEVELOPING.md](docs/DEVELOPING.md)**.

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
│   ├── SubstitutionEngine.kt
│   └── WorkoutEngine.kt
├── model/
│   └── Models.kt
├── session/
│   ├── SessionRunStore.kt
│   └── SessionTiming.kt
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
