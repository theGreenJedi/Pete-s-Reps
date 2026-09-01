package org.petesreps.model

enum class MovementFamily {
    PUSH, PULL, LEGS, TRUNK, MOVEMENT, MOBILITY, CONDITIONING
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
)

data class ExercisePrescription(
    val exercise: Exercise,
    val sets: Int,
    val targetPerSet: Int,
    val blockMinutes: Int,
    val challengeNote: String? = null,
) {
    val totalTarget: Int
        get() = targetPerSet * sets * if (exercise.perSide) 2 else 1
}

data class Workout(
    val dayNumber: Int,
    val cycleNumber: Int,
    val cycleDay: Int,
    val focus: String,
    val plannedMinutes: Int,
    val prescriptions: List<ExercisePrescription>,
)

data class TrainingProfile(
    val dayNumber: Int,
    val challengeByFamily: Map<MovementFamily, Int>,
    val successStreakByFamily: Map<MovementFamily, Int>,
    val failureStreakByFamily: Map<MovementFamily, Int>,
    val lastExerciseByFamily: Map<MovementFamily, String?>,
)

data class TrainingSummary(
    val currentDay: Int,
    val workoutsLogged: Int,
    val challengeByFamily: Map<MovementFamily, Int>,
)
