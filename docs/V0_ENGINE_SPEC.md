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

Named long-term destinations currently include:

- handstand walk
- handstand push-up
- grip strength
- strict pull-ups
- dead hang
- pistol squat
- L-sit
- deep squat mobility
- heavy carry
- rope-climb capability

These destinations are hidden plumbing. They are not user-facing levels or test events.

## Core data

### Exercise

An exercise has:

- stable ID
- display name
- movement family
- internal tier
- objective measure (`REPS` or `SECONDS` in v0)
- base target
- set count
- per-side flag
- equipment metadata
- movement cues
- optional demonstration asset

### TrainingProfile

The engine receives a longitudinal profile containing:

- current session number
- challenge state by movement family
- success streak by family
- underperformance streak by family
- most recent exercise by family
- most recent training session for each family

### Performance history

The current SQLite store records prescribed total, actual total, movement family, unit, challenge state, and completion time for every logged exercise.

## Recovery inference

Recovery is inferred from demonstrated performance rather than self-report forms.

In v0:

- repeatedly meeting prescriptions can advance challenge state
- a material performance drop is recorded as underperformance
- recent underperformance creates a readiness penalty in family selection
- if an affected family is selected again, the next target is reduced quietly
- repeated material underperformance can reduce challenge state

This is intentionally conservative plumbing, not a claim that one number perfectly measures biological recovery.

## Failure

Failure is not programmed as a goal.

The engine records what happened and adapts. v0 currently has objective totals rather than a dedicated failure event type; a richer result model can be added later without changing this rule.

## Session selection

The old fixed seven-session template and 56-day intensity wave are retired.

For each session the engine now:

1. calculates how long it has been since each non-mobility movement family was trained
2. applies small balancing weights for high-transfer domains
3. penalizes very recent exposure
4. penalizes recent underperformance
5. uses a deterministic variety term to avoid unnecessary repetition
6. chooses the three highest-priority non-mobility families
7. appends a mobility/stretching block
8. assigns four blocks totaling 23 minutes (6 + 6 + 6 + 5)

The remaining two minutes are intentional slack for transitions and logging while preserving the 25-minute hard ceiling.

## Exercise selection

Within a selected movement family the engine:

1. derives an appropriate internal tier from challenge state
2. avoids repeating the immediately previous exercise when alternatives exist
3. uses the capability graph to favor broad-transfer movements
4. retains deterministic variety among similarly useful candidates
5. derives the objective target from base target, current challenge, recent success, and inferred readiness

The user does not see any of these scores.

## Equipment

Core equipment:

- pull-up bar
- kettlebell
- medicine ball

Conventional weights are optional and should never become necessary for the core program.

The v0 movement library now contains initial kettlebell, medicine-ball, grip, handstand, L-sit, carry, and stretching entries. Load-specific longitudinal tracking remains a follow-up because the current result model records reps or seconds only.

## User experience

### Before starting

Show the complete session so the user knows what is coming.

### During training

Switch to a focused one-movement-at-a-time view. The full overview remains available on demand.

### After a movement

Ask only for the objective result needed by the current model.

### After the session

Persist the workout and regenerate from the updated longitudinal profile. Do not require a recovery survey, journal entry, rating, or benchmark test.

## Architecture

Current repository structure:

- `app/src/main/java/org/petesreps/model/Models.kt` — shared models
- `app/src/main/java/org/petesreps/data/ExerciseCatalog.kt` — movement library
- `app/src/main/java/org/petesreps/data/TrainingDatabase.kt` — local longitudinal history
- `app/src/main/java/org/petesreps/engine/CapabilityGraph.kt` — hidden transfer/goal graph
- `app/src/main/java/org/petesreps/engine/WorkoutEngine.kt` — session generation
- `app/src/main/java/org/petesreps/ui/PetesRepsScreen.kt` — overview and focused execution UI

The project is still intentionally small. A later refactor may split the engine, models, and data layer into separate Gradle modules and migrate persistence to Room, but those are implementation improvements rather than prerequisites for proving the training engine.

## v0 success condition

v0 succeeds when a user can:

1. install the APK from the GitHub repository build pipeline
2. open the app without creating an account
3. see an entire session before starting
4. execute it one movement at a time
5. finish inside the 25-minute envelope
6. enter minimal objective results
7. return across hundreds of sessions
8. receive progressively adapted work without managing training theory or progression ladders

**Complex engine. Simple interface. Capability first.**
