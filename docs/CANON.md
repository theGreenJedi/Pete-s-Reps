# Pete's Reps Canon

These are design constraints, not suggestions.

## Mission

Pete's Reps is a personal daily physical-training engine. It should make it easy to open the app, see today's work, perform it, record reality, and leave. It is not a social network, content store, calorie tracker, or subscription service.

## Non-negotiables

1. **25-minute hard ceiling.** The engine must solve progression inside the time box. It may prescribe shorter recovery sessions, but it must never create a workout longer than 25 minutes.
2. **Actual tracking.** Prescribed work and completed work are different facts. Persist the actual result.
3. **Long-memory progression.** Day 300 must be informed by days 1-299. A user who progressed beyond three push-ups should not be sent back to three push-ups merely because a template repeats.
4. **Indefinite progression.** Eight-week blocks are cycles, not endings. Day 57 becomes cycle 2 day 1; day 113 becomes cycle 3 day 1; there is no program-complete state.
5. **Structured variety.** Rotate exercises within movement families while retaining enough anchor work to measure improvement. Avoid both monotony and random exercise roulette.
6. **Minimal equipment.** A pull-up bar is the only required purchased exercise equipment. Floor, wall, stairs, or a stable household chair may be treated as environment, not specialized gear.
7. **Android first.** Optimize for Android. Retool for iOS only if that need actually arises.
8. **GitHub is source of truth.** Source, exercise definitions, progression rules, build instructions, and history format must remain recoverable and understandable.
9. **Local first.** No mandatory account, cloud, analytics, advertising, or network dependency.
10. **Demonstration-ready.** Every exercise may attach local video, animation, stills, and/or movement cues. Missing media must never prevent a workout from functioning.

## Training influences

Pete's Reps can learn principles from many traditions without cloning a proprietary program:

- Strength Side: mobility as training, hanging, joint range, bodyweight variety, movement quality.
- U.S. Marine Corps physical training: pull-ups, push-ups, trunk endurance, simple circuits, work capacity, crawls, conditioning, and useful fitness with little equipment.
- Martial arts: stance strength, hip mobility, rotation, balance, footwork, crawling, shrimping, technical stand-ups, controlled sprawling, and moving between floor and standing.
- General calisthenics and gymnastics basics: leverage, unilateral progression, range of motion, tempo, pauses, and body control.

Do not copy another publisher's proprietary schedule, prose, videos, or paid exercise library. Pete's Reps owns its descriptions, selection rules, and adaptation logic.

## Capability families

The engine reasons about at least these persistent domains:

- Push
- Pull
- Legs
- Trunk
- Movement
- Mobility
- Conditioning

The UI does not need to gamify them. The engine does need to remember their training state independently.

## Progression

Progress is performance-driven, not calendar-only. Useful progression dimensions include:

- reps or seconds
- movement tier / leverage
- range of motion
- unilateral work
- controlled eccentric tempo
- pauses
- density inside a fixed block
- reduced rest when appropriate

Repeated successful work raises a family's challenge state. Repeated material failure may temporarily lower it. The underlying challenge state is not artificially capped merely because the current exercise library is finite; new exercise tiers can be added later without resetting history.

## 56-day cycles

A cycle is an organizational lens for build, consolidation, and recovery. It must not erase capability state or exercise history. Cycle rollover is automatic.

## Demonstrations

Text cues ship first. Later, an exercise can point to local media assets. Prefer short, clear, offline demonstrations. External streaming services must not become a core dependency.
