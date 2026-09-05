package org.petesreps.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.petesreps.data.ExerciseCatalog
import org.petesreps.model.ExercisePrescription
import org.petesreps.model.MovementFamily

class SubstitutionEngineTest {
    private val engine = SubstitutionEngine()

    @Test
    fun substitutePreservesBlockAndIntendedStimulus() {
        val original = prescription("wall_handstand", stimulus = MovementFamily.MOVEMENT, minutes = 6)

        val replacement = engine.substitute(original)

        assertNotNull(replacement)
        replacement!!
        assertNotEquals(original.exercise.id, replacement.exercise.id)
        assertEquals(6, replacement.blockMinutes)
        assertEquals(MovementFamily.MOVEMENT, replacement.stimulusFamily)
        assertTrue(replacement.targetPerSet > 0)
        assertTrue(replacement.sets > 0)
    }

    @Test
    fun substituteNeverReturnsAvoidedExercise() {
        val original = prescription("pushup", stimulus = MovementFamily.PUSH)
        val first = engine.substitute(original)!!
        val second = engine.substitute(
            current = first,
            avoidExerciseIds = setOf(original.exercise.id, first.exercise.id),
        )

        assertNotNull(second)
        assertFalse(second!!.exercise.id in setOf(original.exercise.id, first.exercise.id))
        assertEquals(MovementFamily.PUSH, second.stimulusFamily)
    }

    @Test
    fun substituteKeepsRelativePrescriptionDemandReasonable() {
        val original = prescription("dead_hang", stimulus = MovementFamily.PULL, targetPerSet = 24)
        val replacement = engine.substitute(original)!!

        val originalRatio = original.targetPerSet.toDouble() / original.exercise.baseTargetPerSet
        val replacementRatio = replacement.targetPerSet.toDouble() / replacement.exercise.baseTargetPerSet

        assertTrue(kotlin.math.abs(originalRatio - replacementRatio) <= 0.25)
    }

    @Test
    fun restoredSubstituteKeepsExactCheckpointPrescription() {
        val restored = engine.restore(
            exerciseId = "towel_hang",
            sets = 3,
            targetPerSet = 17,
            blockMinutes = 6,
            stimulusFamilyName = "PULL",
        )

        assertNotNull(restored)
        restored!!
        assertEquals("towel_hang", restored.exercise.id)
        assertEquals(3, restored.sets)
        assertEquals(17, restored.targetPerSet)
        assertEquals(6, restored.blockMinutes)
        assertEquals(MovementFamily.PULL, restored.stimulusFamily)
    }

    private fun prescription(
        exerciseId: String,
        stimulus: MovementFamily,
        minutes: Int = 6,
        targetPerSet: Int? = null,
    ): ExercisePrescription {
        val exercise = ExerciseCatalog.all.first { it.id == exerciseId }
        return ExercisePrescription(
            exercise = exercise,
            sets = exercise.defaultSets,
            targetPerSet = targetPerSet ?: exercise.baseTargetPerSet,
            blockMinutes = minutes,
            stimulusFamily = stimulus,
        )
    }
}
