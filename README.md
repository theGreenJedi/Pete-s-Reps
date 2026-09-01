# Pete's Reps

An Android-first, local-first daily training app built to keep progressing instead of ending after eight weeks.

Pete's Reps generates varied bodyweight sessions, records what was actually completed, and adapts future prescriptions from that history. Workouts are hard-capped at **25 minutes**. A pull-up bar is the only required piece of exercise equipment.

## Status

Early experimental build. The first milestone is a small, dependable training engine rather than a large fitness platform.

## Core ideas

- 25 minutes is a hard ceiling, not a target to exceed.
- Training history persists on-device and affects future work.
- Progression changes reps, movement difficulty, leverage, control, range, and density rather than making sessions endlessly longer.
- Structured variety prevents the same handful of exercises from repeating forever.
- Eight-week / 56-day cycles organize training; they do not end it. Day 57 begins the next cycle.
- Strength Side-style mobility and bodyweight ideas, U.S. Marine Corps physical-training traditions, calisthenics, and martial-arts movement are inspirations. Pete's Reps uses original programming, descriptions, and progression logic.
- No account, subscription, cloud service, analytics, or network connection is required.
- Exercise demonstrations are supported by the data model but are not required for the engine to function.

See [docs/CANON.md](docs/CANON.md) for the project rules.

## Android stack

- Kotlin / Jetpack Compose
- Android Gradle Plugin 9.3.0
- Kotlin / Compose compiler plugin 2.4.10
- Compose BOM 2026.08.00
- compileSdk 37, targetSdk 36, minSdk 26
- JDK 17 / Gradle 9.5.0
- SQLite for durable local history

## Build

The repository CI installs Gradle 9.5.0 and the Android SDK, runs unit tests, then builds a debug APK.

Locally, open the project in a current Android Studio release or use Gradle 9.5.0 with JDK 17:

```bash
gradle :app:testDebugUnitTest :app:assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Safety

Pete's Reps is training software, not medical care. Exercise within your ability, use a securely installed pull-up bar, and stop a movement that causes sharp pain, dizziness, or other concerning symptoms.

## License

MIT. See [LICENSE](LICENSE).
