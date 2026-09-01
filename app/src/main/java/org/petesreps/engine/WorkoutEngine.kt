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
    private data class DayPlan(
        val focus: String,
        val families: List<MovementFamily>,
        val minutes: List<Int>,
    )

    private val week = listOf(
        DayPlan("Push / pull strength", listOf(MovementFamily.PUSH, MovementFamily.PULL, MovementFamily.TRUNK, MovementFamily.MOBILITY), listOf(6, 6, 6, 5)),
        DayPlan("Legs / movement", listOf(MovementFamily.LEGS, MovementFamily.MOVEMENT, MovementFamily.MOBILITY, MovementFamily.CONDITIONING), listOf(6, 6, 5, 6)),
        DayPlan("Pull / legs strength", listOf(MovementFamily.PULL, MovementFamily.LEGS, MovementFamily.TRUNK, MovementFamily.MOBILITY), listOf(6, 6, 6, 5)),
        DayPlan("Restore / move", listOf(MovementFamily.MOBILITY, MovementFamily.MOVEMENT, MovementFamily.PULL, MovementFamily.TRUNK), listOf(5, 5, 4, 4)),
        DayPlan("Push / legs strength", listOf(MovementFamily.PUSH, MovementFamily.LEGS, MovementFamily.TRUNK, MovementFamily.MOBILITY), listOf(6, 6, 6, 5)),
        DayPlan("Condition / move", listOf(MovementFamily.CONDITIONING, MovementFamily.MOVEMENT, MovementFamily.PULL, MovementFamily.MOBILITY), listOf(6, 6, 6, 5)),
        DayPlan("Recovery movement", listOf(MovementFamily.MOBILITY, MovementFamily.LEGS, MovementFamily.MOVEMENT, MovementFamily.TRUNK), listOf(5, 5, 4, 4)),
    )

    fun generate(profile: TrainingProfile): Workout {
        val day = profile.dayNumber.coerceAtLeast(1)
        val cycleNumber = ((day - 1) / 56) + 1
        val cycleDay = ((day - 1) % 56) + 1
        val plan = week[(day - 1) % week.size]

        val prescriptions = plan.families.mapIndexed { index, family ->
            prescription(
                family = family,
                day = day,
                cycleDay = cycleDay,
                blockMinutes = plan.minutes[index],
                challenge = profile.challengeByFamily[family] ?: 0,
                streak = profile.successStreakByFamily[family] ?: 0,
                lastExerciseId = profile.lastExerciseByFamily[family],
            )
        }

        val planned = prescriptions.sumOf { it.blockMinutes }
        check(planned <= 25) { "Workout generator violated the 25-minute ceiling: $planned" }

        return Workout(
            dayNumber = day,
            cycleNumber = cycleNumber,
            cycleDay = cycleDay,
            focus = plan.focus,
            plannedMinutes = planned,
            prescriptions = prescriptions,
        )
    }

    private fun prescription(
        family: MovementFamily,
        day: Int,
        cycleDay: Int,
        blockMinutes: Int,
        challenge: Int,
        streak: Int,
        lastExerciseId: String?,
    ): ExercisePrescription {
        val catalog = ExerciseCatalog.forFamily(family)
        val maxTier = catalog.maxOf { it.tier }
        val desiredTier = (1 + challenge.coerceAtLeast(0) / 3).coerceAtMost(maxTier)
        val tierPool = catalog.filter { it.tier == desiredTier }.ifEmpty {
            catalog.filter { it.tier <= desiredTier }.takeLast(2)
        }

        val seed = day * 31 + family.ordinal * 17 + challenge * 7
        var exercise = tierPool[Math.floorMod(seed, tierPool.size)]
        if (exercise.id == lastExerciseId && tierPool.size > 1) {
            exercise = tierPool[(Math.floorMod(seed, tierPool.size) + 1) % tierPool.size]
        }

        val cycleMultiplier = when (cycleDay) {
            in 1..7 -> 0.85
            in 8..14 -> 0.90
            in 15..28 -> 1.00
            in 29..42 -> 1.08
            in 43..49 -> 1.00
            else -> 0.82
        }

        val tierStartChallenge = (desiredTier - 1) * 3
        val withinTier = (challenge - tierStartChallenge).coerceAtLeast(0)
        val targetBump = if (exercise.unit == MeasureUnit.SECONDS) withinTier * 3 else withinTier
        val streakBump = if (streak > 0) 1 else 0
        val target = (exercise.baseTargetPerSet * cycleMultiplier).roundToInt()
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
