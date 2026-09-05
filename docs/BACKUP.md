# Training History Backup & Restore

Pete's Reps is local-first. That means the training engine's most valuable long-term asset — the history of what you actually did and the hidden progression/readiness state inferred from it — lives on the device.

Version 0.3.0 adds an app-owned export/restore path so that history can survive device migration, debug-signing changes, reinstall scenarios, or future storage refactors.

## What a backup contains

A Pete's Reps backup contains all durable training state required to resume the engine:

- completed workout/session rows
- every recorded performance row
- the next session number
- hidden capability challenge state
- success / underperformance streak state
- last movement used by each movement family
- last-trained exposure state used by the rolling session generator
- any retained legacy state keys from earlier builds

It does **not** contain Android credentials, signing keys, account data, analytics identifiers, or cloud tokens. Pete's Reps does not require those for the core training flow.

## Export from the app

From the bottom of the Pete's Reps session screen:

1. Tap **Export training backup**.
2. Choose a location using Android's system file picker.
3. Save the suggested `.preps` file.
4. Keep at least one copy somewhere that will survive loss or replacement of the phone.

The export is generated from the current local database at the time the file is saved.

## Restore into the app

1. Tap **Restore training backup**.
2. Select a Pete's Reps `.preps` backup file.
3. Pete's Reps reads and validates the file before changing the local database.
4. Review the confirmation dialog, including the number of saved sessions in the selected backup.
5. Tap **Restore** only when you intend to replace the history currently on the phone.

Restore is destructive with respect to the current local history: the selected backup becomes the complete local training history. Export the current history first if you may need it later.

## Atomic restore rule

Pete's Reps validates a backup before destructive writes begin. The actual replacement is then performed in a single SQLite transaction.

If validation fails, the existing database is left intact.

If the database transaction fails, SQLite rolls the replacement back instead of leaving a half-restored training history.

## Backup format

The file format is deliberately independent of the SQLite file itself.

Current format header:

```text
PETES_REPS_BACKUP\t1\t<creation-time>
```

Schema version `1` is a line-oriented UTF-8 container. String values are URL-safe Base64 encoded so tabs, punctuation, whitespace, and Unicode movement names remain unambiguous without requiring a third-party serializer.

Record types are:

```text
STATE
WORKOUT
PERFORMANCE
```

The app owns the format. Future schema versions must either remain readable or provide an explicit migration path. Do not silently reinterpret an incompatible backup.

## Compatibility

The first database build used `cycle_number` / `cycle_day` columns for a 56-day training-cycle model. Current builds retain those column names for compatibility but use them as rolling training-week/session metadata.

Backup validation intentionally allows the older positive cycle values so early Pete's Reps history can still be preserved and restored.

## Why this matters before stable signing

Current GitHub Actions downloads are debug APKs. Android may refuse to install one debug build over another if the signing identities differ. Uninstalling the existing app would also remove its private local database.

The backup feature provides a deliberate escape hatch:

1. export training history from the installed build
2. keep the backup outside the app
3. install/reinstall as required
4. restore the backup
5. continue from the recovered progression state

Stable release signing is still the preferred long-term update mechanism; backup/restore is a separate durability layer, not a substitute for consistent signing.

## Recommended cadence

Pete's Reps does not require a backup ritual after every workout. Reasonable checkpoints include:

- before replacing or resetting a phone
- before uninstalling the app
- before switching from debug to stable-release builds
- after a substantial block of accumulated training history
- before testing a storage/database migration

The goal is durability without turning training into data administration.
