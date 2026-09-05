# Pete's Reps — v0 Engine Specification

This document is the implementation-facing description of the v0 training engine. `docs/CANON.md` remains the higher-level source of non-negotiable product rules.

## Product contract

Pete's Reps produces one intelligently selected training session at a time.

- target rhythm: 6 training sessions per week
- hard session cap: 25 minutes total
- primary optimization target: capability
- local-first Android app
- minimal user input
- no benchmark days required
- no visible progression ladders
- no visible readiness/capability scoring
- one-tap movement substitution without a reason questionnaire

## Capability model

The v0 engine reasons about overlapping internal attributes:

- working strength
- grip
- body control
- conditioning
- mobility / flexibility
- balance / coordination
- power
- locomotion
- durability

A movement may contribute to several attributes. `CapabilityGraph.kt` contains the first explicit mapping between exercises, capability weights, and long-term destinations.

Named long-term destinations currently include handstand walk, handstand push-up, grip strength, strict pull-ups, dead hang, pistol squat, L-sit, deep squat mobility, heavy carry, and rope-climb capability.

These destinations are hidden plumbing. They are not user-facing levels or test events.

## Core data

### Exercise

An exercise has a stable ID, display name, movement family, internal tier, objective measure, base target, set count, per-side flag, equipment metadata, movement cues, and optional demonstration asset.

### ExercisePrescription

A prescription contains the actual exercise, sets, objective target, block duration, and a hidden `stimulusFamily`.

Normally `stimulusFamily` equals `exercise.family`. A substitute may use a different exercise family while keeping the block's original intended stimulus. This lets the app record what was actually performed without accidentally moving long-term progression to a different capability bucket.

### TrainingProfile

The engine receives a longitudinal profile containing current session number, challenge state by movement family, success streak by family, underperformance streak by family, most recent exercise by family, and most recent training session for each family.

### Performance history

The current SQLite store records prescribed total, actual total, intended stimulus family, unit, challenge state, and completion time for every logged exercise. `exercise_id` remains the literal movement actually performed.

## Recovery inference

Recovery is inferred from demonstrated performance rather than self-report forms.

In v0, repeatedly meeting prescriptions can advance challenge state; material performance drops create underperformance/readiness penalties; affected targets are reduced quietly; and repeated degradation can reduce challenge state.

## Failure

Failure is not programmed as a goal. The engine records what happened and adapts.

## Session selection

The old fixed seven-session template and 56-day intensity wave are retired.

For each session the engine calculates rolling exposure, applies balancing and readiness weights, chooses three high-priority non-mobility families, appends mobility/stretching, and assigns four blocks totaling 23 minutes (6 + 6 + 6 + 5).

The remaining two minutes are intentional slack for transitions and logging while preserving the 25-minute hard ceiling.

## Exercise selection

Within a selected movement family the engine derives an internal tier, avoids immediate repetition when possible, favors broad-transfer movements using the capability graph, retains deterministic variety, and derives the objective target from challenge and recent performance.

The user does not see these scores.

## One-tap substitution

`SubstitutionEngine.kt` handles real-world movement replacement.

Given the current prescription it scores alternatives using capability overlap, named-goal overlap, intended-family preservation, approximate tier, measurement compatibility, broad transfer, and setup/equipment difference. It avoids exercises already present in the session and, during the current app session, exercises already rejected for that block.

The replacement keeps the original block duration and hidden `stimulusFamily`. Its target is rebuilt from the replacement exercise's normal base target while preserving the approximate relative demand of the original prescription.

A substitute can cross catalog families if that better preserves useful capability transfer or materially changes setup/equipment. The database still advances/adapts the original intended stimulus family while recording the literal replacement exercise ID.

Swapping never resets either the master 25-minute clock or the current block clock.

## Active-session checkpoint

`SessionRunStore.kt` persists the session start, block start, current movement index, entered objective results, and the exact active prescription list.

That exact prescription list is important after 0.5.0: Android activity/process recreation restores a substituted movement instead of regenerating the original one. Older 0.4.x checkpoints without prescription state remain readable and fall back to the generated session.

## Equipment

Core equipment remains pull-up bar, kettlebell, and medicine ball. Conventional weights are optional.

## User experience

### Before starting

Show the complete session. Each movement can be replaced with one tap on **Swap**.

### During training

Show one movement at a time with the master session timer and subordinate movement timer. **Swap** remains available and consumes the time already running; it never restarts the block.

### After a movement

Ask only for the objective result needed by the current model.

### After the session

Persist the workout and regenerate from the updated longitudinal profile. Do not require a recovery survey, journal entry, rating, benchmark test, or explanation for substitutions.

## Architecture

Current repository structure:

- `app/src/main/java/org/petesreps/model/Models.kt` — shared models and intended stimulus field
- `app/src/main/java/org/petesreps/data/ExerciseCatalog.kt` — movement library
- `app/src/main/java/org/petesreps/data/TrainingDatabase.kt` — local longitudinal history
- `app/src/main/java/org/petesreps/engine/CapabilityGraph.kt` — hidden transfer/goal graph
- `app/src/main/java/org/petesreps/engine/SubstitutionEngine.kt` — one-tap replacement scoring/prescription logic
- `app/src/main/java/org/petesreps/engine/WorkoutEngine.kt` — session generation
- `app/src/main/java/org/petesreps/session/SessionRunStore.kt` — active-session/resume checkpoint
- `app/src/main/java/org/petesreps/session/SessionTiming.kt` — hard-clock timing math
- `app/src/main/java/org/petesreps/ui/PetesRepsScreen.kt` — overview and focused execution UI

The project is still intentionally small. A later refactor may split engine/models/data into separate Gradle modules and migrate persistence to Room, but those are implementation improvements rather than prerequisites for proving the training engine.

## v0 success condition

v0 succeeds when a user can install the APK, open without an account, see the whole session, swap an unworkable movement without managing training theory, execute one movement at a time, stop at the 25-minute boundary, enter minimal objective results, and return across hundreds of sessions to progressively adapted work.

**Complex engine. Simple interface. Capability first.**
