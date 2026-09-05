# Pete's Reps Canon

These are design constraints, not suggestions.

## Mission

Pete's Reps is a personal physical-training engine. Open the app, see today's work, perform it, record reality, and leave. It is not a social network, calorie tracker, leaderboard, or subscription service.

The app optimizes for **capability first**. Longevity, working strength, mobility/flexibility, conditioning, body control, balance, coordination, grip, power, locomotion, and durability support that goal.

## Non-negotiables

1. **25-minute hard ceiling.** The entire session, including preparation, training, mobility, stretching, transitions, and logging, must fit inside 25 minutes.
2. **Six training days per week is the target rhythm.** The app programs six sessions; it does not prescribe a hidden seventh-day workout.
3. **Track reality.** Prescribed work and completed work are different facts. Persist what actually happened.
4. **Long-memory progression.** Session 300 must be informed by sessions 1-299. Do not reset a capable user because a template rolled over.
5. **Infer progress from training.** No benchmark days are required. Ordinary sessions supply the evidence.
6. **Failure is an observed event, not a target.** Do not routinely program training to failure. Material underperformance is data for adaptation.
7. **Infer recovery from results.** Do not require sleep, soreness, mood, or RPE questionnaires for the engine to adapt.
8. **Hide progression ladders.** The engine may maintain detailed prerequisites and movement progressions, but the user sees only the next appropriate movement.
9. **Invisible plumbing.** Internal scores, readiness logic, goal relevance, and selection rationale stay out of the default UI.
10. **Structured variety.** Avoid both monotony and random exercise roulette. Prefer movements that solve current capability needs and transfer broadly.
11. **Small core equipment set.** Assume a pull-up bar, kettlebell, and medicine ball. Conventional weights are optional rather than required.
12. **Stretching is training.** Mobility and flexibility are first-class programming concerns, not optional cleanup.
13. **Android first.** Optimize for Android.
14. **GitHub is source of truth.** Source, engine rules, exercise definitions, build instructions, installation instructions, and architecture documentation must remain recoverable.
15. **Local first.** No mandatory account, cloud, analytics, advertising, or network dependency.
16. **Demonstration-ready.** Exercises may attach local video, animation, stills, and/or movement cues. Missing media must never prevent a workout from functioning.
17. **One-tap substitute.** If a movement is not workable today, one tap must choose another movement without interrogating the user. Preserve the intended capability stimulus and remaining time rather than restarting or redesigning the session.
18. **No unexplained movements.** Every prescribed movement must have an immediately available in-app **How** explanation that describes setup and execution in plain language. Technique cues alone are not a substitute for explaining what the movement actually is. Media is optional; understandable text is mandatory.

## Working strength

Pete's Reps prioritizes useful, transferable strength rather than isolated gym numbers. It values grip and forearm strength, pulling and hanging ability, overhead strength, trunk strength, single-leg strength, carries, awkward-load control, strength through useful ranges, and bodyweight-relative strength.

## Named capability destinations

These are internal destinations, not visible levels or mandatory tests:

- handstand walk
- handstand push-up
- exceptional grip strength
- strong strict pull-ups
- long dead hang
- pistol squat
- L-sit
- deep squat mobility
- heavy carry capacity
- rope-climb capability

The engine builds prerequisites quietly over months and presents only the next useful movement.

## Conditioning

Conditioning is broad-spectrum. Pete's Reps should develop sustained 20-30 minute work capacity, repeated sprint/recovery ability, comfortable multi-mile running capability, and longer loaded efforts. The 25-minute session cap remains absolute; the engine develops these outcomes through density, intervals, progression, movement selection, and transfer.

## Training influences

Pete's Reps can learn principles from bodyweight training, mobility work, U.S. Marine Corps physical-training traditions, martial-arts movement, gymnastics basics, kettlebell work, and medicine-ball work without copying another publisher's proprietary schedule, prose, videos, or paid exercise library.

## Hidden capability model

The engine may reason about overlapping internal attributes including working strength, grip, body control, conditioning, mobility/flexibility, balance/coordination, power, locomotion, and durability. One movement may train several attributes at once. Prefer broad-transfer movements when they solve the current training need.

## Session generation

There is no visible body-part split, rigid seven-day template, or program-complete state. Before each session the engine considers recent exposure, recent performance, challenge state, movement progression, variety, broad transfer, long-term goal relevance, equipment, conditioning balance, mobility/stretching exposure, and the remaining 25-minute budget.

The app shows the complete session before starting, then supports a focused one-movement-at-a-time execution view. The full overview remains available on demand.

## Timing enforcement

The 25-minute ceiling is enforced by one live master clock. It starts when the session starts and never resets between movements. Time spent transitioning, logging a result, viewing the overview, backgrounding the app, or with the screen asleep still counts.

At 00:00 the session ends automatically. Save objective results already entered and treat movements with no recorded result as unattempted rather than inventing zero-rep failures.

Each current movement may have a subordinate pacing timer. When that timer expires, the app should tell the user to move on, but the master 25-minute timer remains the sole absolute session boundary.

An in-progress session must resume from its original master-clock start after ordinary activity/process recreation. It must never receive a fresh 25 minutes merely because the UI restarted.

## One-tap substitution

A movement can be rejected without supplying a reason. **Swap** means: choose the closest practical alternative and continue.

The substitution engine should preserve, in order of importance, the intended capability stimulus, meaningful goal transfer, approximate difficulty, and the existing block time. It may choose a movement from another catalog family when that better preserves the useful stimulus or materially changes the setup/equipment requirement.

The block's hidden intended stimulus remains attached to the replacement so long-term progression is not distorted merely because the literal exercise changed. Persist the actual replacement movement as the movement performed.

Swapping must not reset the 25-minute master clock. If the session is already running, it also must not restart the current movement's time allowance. The replacement gets whatever time remains.

Do not ask whether the reason was pain, equipment, preference, space, or something else as part of the normal one-tap path. The app is allowed to fail with a simple statement that no useful substitute is available rather than offering a knowingly poor replacement.

## User input

Ask for the smallest useful amount of objective information, such as reps, seconds, distance, or load when relevant. Infer everything else where practical. Do not require journaling, recovery surveys, motivation ratings, or training-theory decisions from the user.

## Demonstrations

Every catalog movement must ship with a short plain-language text explanation that answers two questions: **How do I set up?** and **What do I actually do?** The app exposes that explanation through **How** both in the pre-session overview and the live movement view. Adding a movement without an explanation is a test failure.

Technique cues are the second layer: short reminders about the important details once the user understands the movement. Later, an exercise can also point to local video, animation, or still assets. Prefer short, clear, offline demonstrations. External streaming services must not become a core dependency, and missing media must never make the movement unknowable.
