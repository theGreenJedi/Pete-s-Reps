package org.petesreps.engine

import org.petesreps.model.Capability
import org.petesreps.model.CapabilityGoal
import org.petesreps.model.Exercise
import org.petesreps.model.MovementFamily

/**
 * Hidden training plumbing. The UI should not expose levels, scores, or progression ladders.
 * This graph exists so the engine can reason about transfer instead of treating exercises as
 * isolated rep targets.
 */
object CapabilityGraph {
    private val familyWeights: Map<MovementFamily, Map<Capability, Int>> = mapOf(
        MovementFamily.PUSH to mapOf(
            Capability.WORKING_STRENGTH to 3,
            Capability.BODY_CONTROL to 2,
            Capability.DURABILITY to 1,
        ),
        MovementFamily.PULL to mapOf(
            Capability.WORKING_STRENGTH to 3,
            Capability.GRIP to 3,
            Capability.BODY_CONTROL to 1,
        ),
        MovementFamily.LEGS to mapOf(
            Capability.WORKING_STRENGTH to 3,
            Capability.BALANCE_COORDINATION to 1,
            Capability.DURABILITY to 2,
        ),
        MovementFamily.TRUNK to mapOf(
            Capability.WORKING_STRENGTH to 2,
            Capability.BODY_CONTROL to 3,
            Capability.DURABILITY to 1,
        ),
        MovementFamily.MOVEMENT to mapOf(
            Capability.BODY_CONTROL to 3,
            Capability.BALANCE_COORDINATION to 3,
            Capability.LOCOMOTION to 2,
        ),
        MovementFamily.MOBILITY to mapOf(
            Capability.MOBILITY_FLEXIBILITY to 4,
            Capability.DURABILITY to 2,
            Capability.BODY_CONTROL to 1,
        ),
        MovementFamily.CONDITIONING to mapOf(
            Capability.CONDITIONING to 4,
            Capability.POWER to 2,
            Capability.DURABILITY to 1,
        ),
    )

    private val exerciseWeights: Map<String, Map<Capability, Int>> = mapOf(
        "dead_hang" to mapOf(Capability.GRIP to 5, Capability.DURABILITY to 2),
        "towel_hang" to mapOf(Capability.GRIP to 6, Capability.WORKING_STRENGTH to 2),
        "pullup" to mapOf(Capability.WORKING_STRENGTH to 5, Capability.GRIP to 4, Capability.BODY_CONTROL to 2),
        "pause_pullup" to mapOf(Capability.WORKING_STRENGTH to 5, Capability.GRIP to 4, Capability.BODY_CONTROL to 3),
        "wall_handstand" to mapOf(Capability.BODY_CONTROL to 6, Capability.BALANCE_COORDINATION to 4, Capability.WORKING_STRENGTH to 2),
        "handstand_weight_shift" to mapOf(Capability.BODY_CONTROL to 6, Capability.BALANCE_COORDINATION to 5, Capability.WORKING_STRENGTH to 3),
        "wall_hspu_negative" to mapOf(Capability.WORKING_STRENGTH to 6, Capability.BODY_CONTROL to 5, Capability.BALANCE_COORDINATION to 3),
        "tuck_lsit" to mapOf(Capability.BODY_CONTROL to 5, Capability.WORKING_STRENGTH to 3),
        "assisted_pistol" to mapOf(Capability.WORKING_STRENGTH to 4, Capability.BALANCE_COORDINATION to 4, Capability.MOBILITY_FLEXIBILITY to 2),
        "deep_squat_hold" to mapOf(Capability.MOBILITY_FLEXIBILITY to 6, Capability.DURABILITY to 2),
        "kb_suitcase_carry" to mapOf(Capability.GRIP to 5, Capability.WORKING_STRENGTH to 4, Capability.CONDITIONING to 2, Capability.BODY_CONTROL to 2),
        "kb_swing" to mapOf(Capability.POWER to 5, Capability.CONDITIONING to 4, Capability.WORKING_STRENGTH to 3),
        "medicine_ball_slam" to mapOf(Capability.POWER to 5, Capability.CONDITIONING to 4, Capability.BODY_CONTROL to 2),
        "bear_steps" to mapOf(Capability.LOCOMOTION to 4, Capability.BODY_CONTROL to 3, Capability.CONDITIONING to 1),
        "crawl_interval" to mapOf(Capability.LOCOMOTION to 4, Capability.CONDITIONING to 4, Capability.BODY_CONTROL to 2),
    )

    private val goalLinks: Map<String, Set<CapabilityGoal>> = mapOf(
        "wall_handstand" to setOf(CapabilityGoal.HANDSTAND_WALK, CapabilityGoal.HANDSTAND_PUSHUP),
        "handstand_weight_shift" to setOf(CapabilityGoal.HANDSTAND_WALK, CapabilityGoal.HANDSTAND_PUSHUP),
        "wall_hspu_negative" to setOf(CapabilityGoal.HANDSTAND_PUSHUP),
        "pike_pushup" to setOf(CapabilityGoal.HANDSTAND_PUSHUP),
        "dead_hang" to setOf(CapabilityGoal.DEAD_HANG, CapabilityGoal.GRIP_STRENGTH, CapabilityGoal.ROPE_CLIMB),
        "towel_hang" to setOf(CapabilityGoal.GRIP_STRENGTH, CapabilityGoal.ROPE_CLIMB),
        "pullup" to setOf(CapabilityGoal.STRICT_PULLUPS, CapabilityGoal.ROPE_CLIMB),
        "pause_pullup" to setOf(CapabilityGoal.STRICT_PULLUPS, CapabilityGoal.ROPE_CLIMB),
        "tuck_lsit" to setOf(CapabilityGoal.L_SIT),
        "assisted_pistol" to setOf(CapabilityGoal.PISTOL_SQUAT),
        "deep_squat_hold" to setOf(CapabilityGoal.DEEP_SQUAT_MOBILITY),
        "kb_suitcase_carry" to setOf(CapabilityGoal.HEAVY_CARRY, CapabilityGoal.GRIP_STRENGTH),
    )

    fun weightsFor(exercise: Exercise): Map<Capability, Int> {
        val merged = familyWeights.getValue(exercise.family).toMutableMap()
        exerciseWeights[exercise.id]?.forEach { (capability, weight) ->
            merged[capability] = maxOf(merged[capability] ?: 0, weight)
        }
        return merged
    }

    fun goalsFor(exercise: Exercise): Set<CapabilityGoal> = goalLinks[exercise.id].orEmpty()

    /** A small hidden preference for movements that train several useful qualities at once. */
    fun transferScore(exercise: Exercise): Int =
        weightsFor(exercise).values.sum() + goalsFor(exercise).size * 2
}
