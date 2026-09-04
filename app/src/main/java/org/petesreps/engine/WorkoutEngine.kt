package org.petesreps.engine

import kotlin.math.roundToInt
import org.petesreps.data.ExerciseCatalog
import org.petesreps.model.Exercise
import org.petesreps.model.ExercisePrescription
import org.petesreps.model.MeasureUnit
import org.petesreps.model.MovementFamily
import org.petesreps.model.TrainingProfile
import org.petesreps.model.Workout

class WorkoutEngine {
    fun generate(profile: TrainingProfile): Workout {
        val day = profile.dayNumber.coerceAtLeast(1)
        val families = selectFamilies(profile, day)
        val blockMinutes = listOf(6, 6, 6, 5)

        val prescriptions = families.mapIndexed { index, family ->
            prescription(
                family = family,
                day = day,
                blockMinutes = blockMinutes[index],
                challenge = profile.challengeByFamily[family] ?: 0,
                successStreak = profile.successStreakByFamily[family] ?: 0,
                underperformanceStreak = profile.underperformanceStreakByFamily[family] ?: 0,
                lastExerciseId = profile.lastExerciseByFamily[family],
            )
        }

        val planned = prescriptions.sumOf { it.blockMinutes }
        check(planned <= 25) { "Workout generator violated the 25-minute ceiling: $planned" }

        return Workout(
            dayNumber = day,
            // These fields remain for backward-compatible persistence. They now describe a
            // rolling six-session training rhythm, not a 56-day program cycle.
            cycleNumber = ((day - 1) / 6) + 1,
            cycleDay = ((day - 1) % 6) + 1,
            focus = "Capability session",
            plannedMinutes = planned,
            prescriptions = prescriptions,
        )
    }

    /**
     * Choose what needs useful exposure now. There is intentionally no visible body-part split.
     * Three non-mobility families are selected from rolling history; mobility/stretching is a
     * first-class part of every v0 session and consumes the final five-minute block.
     */
    private fun selectFamilies(profile: TrainingProfile, day: Int): List<MovementFamily> {
        val selected = MovementFamily.entries
            .filterNot { it == MovementFamily.MOBILITY }
            .sortedWith(
                compareByDescending<MovementFamily> { familyPriority(it, profile, day) }
                    .thenBy { it.ordinal },
            )
            .take(3)
            .sortedBy(::executionOrder)
            .toMutableList()

        selected += MovementFamily.MOBILITY
        return selected
    }

    private fun familyPriority(family: MovementFamily, profile: TrainingProfile, day: Int): Int {
        val lastTrained = profile.lastTrainedDayByFamily[family] ?: 0
        val sessionsSince = if (lastTrained <= 0) day + 6 else (day - lastTrained).coerceAtLeast(0)
        val underperformance = profile.underperformanceStreakByFamily[family] ?: 0

        val balanceBoost = when (family) {
            MovementFamily.PULL -> 8 // grip and pulling are high-transfer capability anchors
            MovementFamily.MOVEMENT -> 7
            MovementFamily.CONDITIONING -> 7
            MovementFamily.LEGS -> 6
            MovementFamily.TRUNK -> 5
            MovementFamily.PUSH -> 5
            MovementFamily.MOBILITY -> 0
        }
        val recentPenalty = if (sessionsSince <= 1) 18 else 0
        val readinessPenalty = underperformance * 20
        val deterministicVariety = Math.floorMod(day * 13 + family.ordinal * 7, 9)

        return sessionsSince.coerceAtMost(12) * 12 + balanceBoost + deterministicVariety - recentPenalty - readinessPenalty
    }

    private fun executionOrder(family: MovementFamily): Int = when (family) {
        MovementFamily.MOVEMENT -> 0
        MovementFamily.PUSH, MovementFamily.PULL, MovementFamily.LEGS, MovementFamily.TRUNK -> 1
        MovementFamily.CONDITIONING -> 2
        MovementFamily.MOBILITY -> 3
    }

    private fun prescription(
        family: MovementFamily,
        day: Int,
        blockMinutes: Int,
        challenge: Int,
        successStreak: Int,
        underperformanceStreak: Int,
        lastExerciseId: String?,
    ): ExercisePrescription {
        val catalog = ExerciseCatalog.forFamily(family)
        val maxTier = catalog.maxOf { it.tier }
        val desiredTier = (1 + challenge.coerceAtLeast(0) / 3).coerceAtMost(maxTier)
        val tierPool = catalog.filter { it.tier == desiredTier }.ifEmpty {
            catalog.filter { it.tier <= desiredTier }.takeLast(3)
        }

        val candidates = tierPool.filterNot { it.id == lastExerciseId }.ifEmpty { tierPool }
        val bestTransfer = candidates.maxOf(CapabilityGraph::transferScore)
        val highTransferPool = candidates.filter { CapabilityGraph.transferScore(it) >= bestTransfer - 2 }
        val seed = day * 31 + family.ordinal * 17 + challenge * 7
        val exercise = highTransferPool[Math.floorMod(seed, highTransferPool.size)]

        val tierStartChallenge = (desiredTier - 1) * 3
        val withinTier = (challenge - tierStartChallenge).coerceAtLeast(0)
        val targetBump = if (exercise.unit == MeasureUnit.SECONDS) withinTier * 3 else withinTier
        val streakBump = if (successStreak > 0) 1 else 0
        val readinessMultiplier = if (underperformanceStreak > 0) 0.85 else 1.0
        val target = (exercise.baseTargetPerSet * readinessMultiplier).roundToInt()
            .coerceAtLeast(1) + targetBump + streakBump

        val overflow = (challenge - maxTier * 3).coerceAtLeast(0)
        val challengeNote = when {
            overflow >= 9 -> "Keep the same time block; favor strict range, slower lowering, and less idle rest."
            overflow >= 6 -> "Add a deliberate pause at the hardest position when form stays clean."
            overflow >= 3 -> "Use controlled three-second lowering before adding more reps."
            else -> null
        }

        return ExercisePrescription(
            exercise = exercise,
            sets = exercise.defaultSets,
            targetPerSet = target,
            blockMinutes = blockMinutes,
            challengeNote = challengeNote,
        )
    }
}
