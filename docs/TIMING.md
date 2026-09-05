# Time Is Law — Session Timing Contract

Pete's Reps has one absolute session limit: **25:00 total**.

The clock starts when the user taps **Start 25:00 session**. It does not reset between movements and it does not pause because the user opens the session overview, backgrounds the app, turns off the screen, or spends time entering a result.

## Master clock

The master session timer uses Android's monotonic elapsed-realtime timebase rather than decrementing a UI counter. The UI periodically recomputes time remaining from the saved session-start timestamp.

This means UI stalls, recomposition timing, screen rotation, and ordinary app backgrounding cannot manufacture extra training time.

At **00:00**:

1. the active session ends automatically
2. objective results already entered are saved
3. movements with no entered result are treated as unattempted rather than converted into false zero-rep failures
4. the in-progress checkpoint is cleared
5. the engine advances to the next session

A user may finish earlier after completing the final movement. Twenty-five minutes is a ceiling, not a minimum.

## Movement clock

Each movement receives a subordinate countdown based on its programmed block duration.

The movement clock starts when that movement becomes current. If it reaches zero, the UI changes to **MOVE ON**. This timer is guidance for pacing; only the master 25:00 clock is the absolute session boundary.

Moving to another exercise starts that exercise's subordinate timer. Opening and closing the full-session overview does not reset the current movement timer.

## Resume behavior

While a session is active, Pete's Reps checkpoints locally:

- session number
- master-clock start timestamp
- current movement
- current movement start timestamp
- objective results already entered

If the activity is recreated or the app process is restored during the same device boot, the session resumes from the original master-clock start. It does not receive a fresh 25 minutes.

If a persisted elapsed-realtime timestamp is no longer valid because the device rebooted, the timer fails closed: the old session is treated as expired rather than restarted.

## Testing

The timing arithmetic is Android-independent and covered by JVM unit tests for:

- exact 25:00 start
- elapsed-time calculation
- hard stop at 25:00
- non-negative countdown behavior
- subordinate block timing
- fail-closed handling of a previous-boot timestamp

Compose instrumentation tests cover the visible master-clock and movement-expiry states. CI compiles those device tests on every change in addition to running the deterministic JVM timing suite.
