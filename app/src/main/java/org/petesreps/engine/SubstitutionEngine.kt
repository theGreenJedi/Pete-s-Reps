package org.petesreps.engine

import kotlin.math.abs
import kotlin.math.roundToInt
import org.petesreps.data.ExerciseCatalog
import org.petesreps.model.Capability
import org.petesreps.model.Exercise
import org.petesreps.model.ExercisePrescription

/**
 * Hidden one-tap substitution plumbing.
 *
 * The user is never asked to classify why a movement is not workable. The
 * engine simply chooses a meaningfully different movement that preserves as
 * much of the current block's capability stimulus as practical.
 */
class SubstitutionEngine(
    private val catalog: List<Exercise> = ExerciseCatalog.all,
) {
    fun substitute(
        current: ExercisePrescription,
        avoidExerciseIds: Set<String> = emptySet(),
    ): ExercisePrescription? {
        val blocked = avoidExerciseIds + current.exercise.id
        val candidates = catalog.filterNot { it.id in blocked }
        if (candidates.isEmpty()) return null

        val best = candidates.maxWithOrNull(
            compareBy<Exercise> { score(current, it) }
                .thenByDescending { it.id },
        ) ?: return null

        return prescriptionFor(current, best)
    }

    /** Rebuild an exact persisted substitute after process/activity recreation. */
    fun restore(
        exerciseId: String,
        sets: Int,
        targetPerSet: Int,
        blockMinutes: Int,
        stimulusFamilyName: String,
    ): ExercisePrescription? {
        val exercise = catalog.firstOrNull { it.id == exerciseId } ?: return null
        val stimulusFamily = runCatching {
            org.petesreps.model.MovementFamily.valueOf(stimulusFamilyName)
        }.getOrNull() ?: exercise.family
        return ExercisePrescription(
            exercise = exercise,
            sets = sets.coerceAtLeast(1),
            targetPerSet = targetPerSet.coerceAtLeast(1),
            blockMinutes = blockMinutes.coerceAtLeast(1),
            stimulusFamily = stimulusFamily,
        )
    }

    private fun prescriptionFor(
        current: ExercisePrescription,
        replacement: Exercise,
    ): ExercisePrescription {
        val relativeDemand = current.targetPerSet.toDouble() /
            current.exercise.baseTargetPerSet.coerceAtLeast(1).toDouble()
        val replacementTarget = (replacement.baseTargetPerSet * relativeDemand)
            .roundToInt()
            .coerceAtLeast(1)

        return ExercisePrescription(
            exercise = replacement,
            sets = replacement.defaultSets,
            targetPerSet = replacementTarget,
            blockMinutes = current.blockMinutes,
            stimulusFamily = current.stimulusFamily,
            challengeNote = null,
        )
    }

    private fun score(current: ExercisePrescription, candidate: Exercise): Int {
        val originalWeights = CapabilityGraph.weightsFor(current.exercise)
        val candidateWeights = CapabilityGraph.weightsFor(candidate)
        val capabilityOverlap = Capability.entries.sumOf { capability ->
            minOf(originalWeights[capability] ?: 0, candidateWeights[capability] ?: 0)
        }
        val originalGoals = CapabilityGraph.goalsFor(current.exercise)
        val candidateGoals = CapabilityGraph.goalsFor(candidate)
        val goalOverlap = originalGoals.intersect(candidateGoals).size

        val intendedFamilyBonus = if (candidate.family == current.stimulusFamily) 28 else 0
        val currentFamilyBonus = if (candidate.family == current.exercise.family) 10 else 0
        val unitBonus = if (candidate.unit == current.exercise.unit) 5 else 0
        val sideBonus = if (candidate.perSide == current.exercise.perSide) 2 else 0
        val tierPenalty = abs(candidate.tier - current.exercise.tier) * 4

        val originalEquipment = current.exercise.equipment
        val candidateEquipment = candidate.equipment
        val equipmentDifference = when {
            originalEquipment.isEmpty() -> 0
            candidateEquipment.isEmpty() -> 16
            candidateEquipment.intersect(originalEquipment).isEmpty() -> 14
            candidateEquipment == originalEquipment -> -18
            else -> -6
        }

        return capabilityOverlap * 7 +
            goalOverlap * 24 +
            intendedFamilyBonus +
            currentFamilyBonus +
            unitBonus +
            sideBonus +
            equipmentDifference +
            CapabilityGraph.transferScore(candidate) -
            tierPenalty
    }
}
