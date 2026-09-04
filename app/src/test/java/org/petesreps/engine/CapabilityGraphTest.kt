package org.petesreps.engine

import org.junit.Assert.assertTrue
import org.junit.Test
import org.petesreps.data.ExerciseCatalog
import org.petesreps.model.Capability
import org.petesreps.model.CapabilityGoal

class CapabilityGraphTest {
    @Test
    fun towelHangAdvancesGripAndRopeClimbCapabilities() {
        val exercise = ExerciseCatalog.all.first { it.id == "towel_hang" }
        assertTrue((CapabilityGraph.weightsFor(exercise)[Capability.GRIP] ?: 0) >= 5)
        assertTrue(CapabilityGoal.ROPE_CLIMB in CapabilityGraph.goalsFor(exercise))
    }

    @Test
    fun handstandWorkConnectsToNamedAspirations() {
        val exercise = ExerciseCatalog.all.first { it.id == "handstand_weight_shift" }
        val goals = CapabilityGraph.goalsFor(exercise)
        assertTrue(CapabilityGoal.HANDSTAND_WALK in goals)
        assertTrue(CapabilityGoal.HANDSTAND_PUSHUP in goals)
    }

    @Test
    fun suitcaseCarryHasBroadTransfer() {
        val exercise = ExerciseCatalog.all.first { it.id == "kb_suitcase_carry" }
        val weights = CapabilityGraph.weightsFor(exercise)
        assertTrue((weights[Capability.GRIP] ?: 0) >= 5)
        assertTrue((weights[Capability.WORKING_STRENGTH] ?: 0) >= 4)
        assertTrue(CapabilityGoal.HEAVY_CARRY in CapabilityGraph.goalsFor(exercise))
    }
}
