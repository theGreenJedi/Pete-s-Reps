package org.petesreps.model

enum class MovementFamily {
    PUSH, PULL, LEGS, TRUNK, MOVEMENT, MOBILITY, CONDITIONING
}

enum class Capability {
    WORKING_STRENGTH,
    GRIP,
    BODY_CONTROL,
    CONDITIONING,
    MOBILITY_FLEXIBILITY,
    BALANCE_COORDINATION,
    POWER,
    LOCOMOTION,
    DURABILITY,
}

enum class CapabilityGoal {
    HANDSTAND_WALK,
    HANDSTAND_PUSHUP,
    GRIP_STRENGTH,
    STRICT_PULLUPS,
    DEAD_HANG,
    PISTOL_SQUAT,
    L_SIT,
    DEEP_SQUAT_MOBILITY,
    HEAVY_CARRY,
    ROPE_CLIMB,
}

enum class Equipment {
    PULL_UP_BAR,
    KETTLEBELL,
    MEDICINE_BALL,
    OPTIONAL_WEIGHTS,
}

enum class MeasureUnit {
    REPS, SECONDS
}

data class Exercise(
    val id: String,
    val name: String,
    val family: MovementFamily,
    val tier: Int,
    val unit: MeasureUnit,
    val baseTargetPerSet: Int,
    val defaultSets: Int,
    val perSide: Boolean = false,
    val pullUpBar: Boolean = false,
    val cues: List<String> = emptyList(),
    val demoAsset: String? = null,
    val equipment: Set<Equipment> = emptySet(),
)

data class ExercisePrescription(
    val exercise: Exercise,
    val sets: Int,
    val targetPerSet: Int,
    val blockMinutes: Int,
    /**
     * Capability bucket this block was selected to train. Normally this is the
     * exercise family. A one-tap substitute may use a different movement while
     * preserving the original block's intended training stimulus.
     */
    val stimulusFamily: MovementFamily = exercise.family,
    val challengeNote: String? = null,
) {
    val totalTarget: Int
        get() = targetPerSet * sets * if (exercise.perSide) 2 else 1
}

data class Workout(
    val dayNumber: Int,
    /** Legacy persisted field; v0 now treats this as the rolling six-session training week. */
    val cycleNumber: Int,
    /** Legacy persisted field; values are now 1..6 rather than a 56-day program cycle. */
    val cycleDay: Int,
    val focus: String,
    val plannedMinutes: Int,
    val prescriptions: List<ExercisePrescription>,
)

data class TrainingProfile(
    val dayNumber: Int,
    val challengeByFamily: Map<MovementFamily, Int>,
    val successStreakByFamily: Map<MovementFamily, Int>,
    val underperformanceStreakByFamily: Map<MovementFamily, Int>,
    val lastExerciseByFamily: Map<MovementFamily, String?>,
    val lastTrainedDayByFamily: Map<MovementFamily, Int> = emptyMap(),
)

data class TrainingSummary(
    val currentDay: Int,
    val workoutsLogged: Int,
    val challengeByFamily: Map<MovementFamily, Int>,
)
