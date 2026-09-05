# One-Tap Substitution

Pete's Reps 0.5.0 adds a simple rule for real-world training: if the prescribed movement is not workable today, tap **Swap** once and continue.

## User contract

- no reason is required
- no pain/equipment/preference questionnaire appears
- the replacement is chosen immediately
- the 25:00 master clock never resets
- if training is already underway, the current movement timer also does not reset
- the replacement receives only the time that remains in the current block

The same **Swap** action is available from the pre-session overview and from the focused current-movement screen.

## Hidden engine behavior

The substitution engine scores alternatives using the existing capability graph rather than treating exercise names as interchangeable labels.

It prefers, in broad order:

1. overlap with the capabilities trained by the current movement
2. overlap with named long-term goals
3. preservation of the block's intended movement family/stimulus
4. comparable difficulty
5. a meaningfully different setup/equipment requirement when useful
6. avoidance of movements already present in the same session

The user does not see these scores.

## Intended stimulus vs. literal movement

A generated block now retains a hidden `stimulusFamily` separately from the exercise that happens to execute it.

Normally:

```text
stimulusFamily == exercise.family
```

After a cross-family substitution, those values may differ.

Example conceptually:

```text
planned block: pulling/grip stimulus
actual movement: a different high-transfer grip/working-strength task
```

Longitudinal adaptation still updates the originally intended stimulus family, while the performance history records the exercise that was actually performed.

This avoids a substitution accidentally rewriting the session plan after the fact.

## Prescription scaling

A replacement keeps the same block duration. Its set/target prescription is rebuilt from the replacement movement's normal prescription while preserving the approximate relative demand of the original prescription.

This allows a substitution to change from reps to seconds or between exercises with very different normal rep counts without blindly copying an inappropriate raw number.

## Checkpoint / resume

An active session checkpoint stores the exact replacement prescriptions alongside:

- original session start time
- current movement index
- current movement start time
- objective results already entered

If Android recreates the activity/process, Pete's Reps restores the replacement movement rather than silently reverting to the original prescription.

Older 0.4.x in-progress checkpoints that do not contain replacement prescriptions remain readable; they simply resume the generated movements.

## Repeated swaps

Within a running app session, previously rejected movements for that block are avoided so repeated taps continue searching rather than immediately bouncing between two choices.

The engine may return a movement from another catalog family when that better preserves useful capability transfer or materially changes the setup. The hidden intended stimulus remains unchanged.

## Failure behavior

If there is no useful candidate, Pete's Reps says so instead of knowingly serving a poor substitute.

## Data semantics

When the workout is saved:

- `exercise_id` is the movement actually performed
- the performance `family` is the block's intended stimulus family
- challenge/progression state is updated against that intended family

This distinction is deliberate and is part of the invisible-plumbing design.
