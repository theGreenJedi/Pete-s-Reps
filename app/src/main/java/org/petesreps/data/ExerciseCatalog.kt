package org.petesreps.data

import org.petesreps.model.Exercise
import org.petesreps.model.MeasureUnit
import org.petesreps.model.MovementFamily

object ExerciseCatalog {
    private fun reps(
        id: String,
        name: String,
        family: MovementFamily,
        tier: Int,
        target: Int,
        sets: Int = 3,
        perSide: Boolean = false,
        bar: Boolean = false,
        vararg cues: String,
    ) = Exercise(id, name, family, tier, MeasureUnit.REPS, target, sets, perSide, bar, cues.toList())

    private fun seconds(
        id: String,
        name: String,
        family: MovementFamily,
        tier: Int,
        target: Int,
        sets: Int = 2,
        perSide: Boolean = false,
        bar: Boolean = false,
        vararg cues: String,
    ) = Exercise(id, name, family, tier, MeasureUnit.SECONDS, target, sets, perSide, bar, cues.toList())

    val all: List<Exercise> = listOf(
        // Push
        reps("wall_pushup", "Wall push-up", MovementFamily.PUSH, 1, 5, cues = arrayOf("Body stays straight", "Lower under control", "Press the wall away")),
        reps("incline_pushup", "Incline push-up", MovementFamily.PUSH, 1, 5, cues = arrayOf("Use a stable surface", "Chest moves toward the edge", "Keep ribs controlled")),
        reps("knee_pushup", "Knee push-up", MovementFamily.PUSH, 2, 6, cues = arrayOf("Straight line from knees to shoulders", "Hands under or just outside shoulders")),
        reps("pushup", "Push-up", MovementFamily.PUSH, 3, 6, cues = arrayOf("Brace trunk", "Chest and hips rise together", "Finish with straight arms")),
        reps("pause_pushup", "Pause push-up", MovementFamily.PUSH, 4, 5, cues = arrayOf("Pause just above the floor", "Do not relax at the bottom", "Press smoothly")),
        reps("decline_pushup", "Decline push-up", MovementFamily.PUSH, 5, 5, cues = arrayOf("Feet on a stable low surface", "Keep the body rigid")),
        reps("diamond_pushup", "Close-grip push-up", MovementFamily.PUSH, 5, 5, cues = arrayOf("Keep elbows controlled", "Use a hand width that feels natural")),
        reps("pike_pushup", "Pike push-up", MovementFamily.PUSH, 6, 5, cues = arrayOf("Hips high", "Head travels forward and down", "Press the floor away")),
        reps("archer_pushup", "Archer push-up", MovementFamily.PUSH, 7, 3, perSide = true, cues = arrayOf("Shift toward the working arm", "Keep the opposite arm long", "Use only a pain-free range")),

        // Pull - the bar is intentionally the only required exercise equipment.
        seconds("dead_hang", "Dead hang", MovementFamily.PULL, 1, 15, sets = 3, bar = true, cues = arrayOf("Use a secure bar", "Long arms", "Step down before grip fails")),
        reps("scap_pull", "Scapular pull", MovementFamily.PULL, 1, 5, bar = true, cues = arrayOf("Keep elbows straight", "Pull shoulders down away from ears")),
        reps("flexed_hang", "Flexed-arm hang", MovementFamily.PULL, 2, 8, sets = 3, bar = true, cues = arrayOf("Use a step to start safely", "Hold with chin near bar", "Lower under control")),
        reps("negative_pullup", "Negative pull-up", MovementFamily.PULL, 3, 3, bar = true, cues = arrayOf("Start above the bar", "Lower for several controlled seconds", "Step back up instead of jumping")),
        reps("chinup", "Chin-up", MovementFamily.PULL, 4, 3, bar = true, cues = arrayOf("Start from a controlled hang", "Drive elbows down", "Avoid swinging")),
        reps("pullup", "Pull-up", MovementFamily.PULL, 5, 3, bar = true, cues = arrayOf("Brace before pulling", "Clear the bar without craning the neck", "Lower with control")),
        reps("pause_pullup", "Pause pull-up", MovementFamily.PULL, 6, 3, bar = true, cues = arrayOf("Pause near the top", "Keep ribs controlled", "No kip")),
        reps("chest_to_bar", "Chest-to-bar pull-up", MovementFamily.PULL, 7, 2, bar = true, cues = arrayOf("Pull high without jerking", "Keep the descent controlled")),

        // Legs
        reps("chair_squat", "Chair squat", MovementFamily.LEGS, 1, 8, cues = arrayOf("Touch the chair lightly", "Stand without rocking for momentum")),
        reps("bodyweight_squat", "Bodyweight squat", MovementFamily.LEGS, 2, 8, cues = arrayOf("Use a comfortable stance", "Knees track with toes", "Own the depth you have")),
        reps("reverse_lunge", "Reverse lunge", MovementFamily.LEGS, 3, 6, perSide = true, cues = arrayOf("Step back softly", "Stay tall", "Drive through the front foot")),
        reps("split_squat", "Split squat", MovementFamily.LEGS, 3, 6, perSide = true, cues = arrayOf("Feet stay planted", "Lower vertically", "Use support for balance if needed")),
        reps("lateral_lunge", "Lateral lunge", MovementFamily.LEGS, 4, 5, perSide = true, cues = arrayOf("Sit into the working hip", "Keep the other leg long")),
        reps("cossack_squat", "Cossack squat", MovementFamily.LEGS, 5, 4, perSide = true, cues = arrayOf("Move only through a controlled range", "Keep the working foot planted")),
        reps("bulgarian_split", "Rear-foot-elevated split squat", MovementFamily.LEGS, 6, 5, perSide = true, cues = arrayOf("Use a stable low support", "Most pressure stays through the front foot")),
        reps("assisted_pistol", "Assisted pistol squat", MovementFamily.LEGS, 7, 3, perSide = true, cues = arrayOf("Hold stable support", "Control the descent", "Use assistance, not momentum")),

        // Trunk
        reps("dead_bug", "Dead bug", MovementFamily.TRUNK, 1, 6, perSide = true, cues = arrayOf("Keep low back controlled", "Move slowly", "Exhale as the leg extends")),
        seconds("front_plank", "Front plank", MovementFamily.TRUNK, 2, 20, sets = 3, cues = arrayOf("Squeeze glutes", "Keep ribs down", "Breathe behind the brace")),
        seconds("side_plank", "Side plank", MovementFamily.TRUNK, 3, 15, sets = 2, perSide = true, cues = arrayOf("Stack shoulders", "Keep hips lifted", "Use the knee-down version if needed")),
        reps("hollow_rock", "Hollow-body rock", MovementFamily.TRUNK, 4, 8, cues = arrayOf("Keep the hollow shape", "Make the rock small and controlled")),
        reps("hanging_knee_raise", "Hanging knee raise", MovementFamily.TRUNK, 5, 5, bar = true, cues = arrayOf("Start from a quiet hang", "Curl knees toward ribs", "Avoid swinging")),
        reps("hanging_leg_raise", "Hanging leg raise", MovementFamily.TRUNK, 6, 4, bar = true, cues = arrayOf("Use the range you can control", "Minimize swing")),
        reps("toes_toward_bar", "Toes-toward-bar raise", MovementFamily.TRUNK, 7, 3, bar = true, cues = arrayOf("Raise with trunk control", "Do not chase the bar with momentum")),

        // Martial-arts and ground-movement influence
        reps("technical_standup", "Technical stand-up", MovementFamily.MOVEMENT, 1, 4, perSide = true, cues = arrayOf("Post securely", "Keep space as you bring the leg through", "Stand under control")),
        reps("bear_steps", "Bear crawl steps", MovementFamily.MOVEMENT, 1, 8, cues = arrayOf("Keep knees close to the floor", "Move opposite hand and foot", "Take quiet steps")),
        reps("shrimp", "Ground shrimp", MovementFamily.MOVEMENT, 2, 5, perSide = true, cues = arrayOf("Bridge lightly", "Turn to one side", "Slide the hips away")),
        reps("crab_steps", "Crab walk steps", MovementFamily.MOVEMENT, 2, 8, cues = arrayOf("Keep shoulders comfortable", "Move smoothly rather than racing")),
        reps("stance_switch", "Fighting-stance switch", MovementFamily.MOVEMENT, 3, 8, perSide = true, cues = arrayOf("Stay balanced", "Land quietly", "Hands remain relaxed and ready")),
        reps("sprawl_to_stand", "Controlled sprawl to stand", MovementFamily.MOVEMENT, 4, 4, cues = arrayOf("Place hands before sending feet back", "Keep the landing controlled", "Stand with balance")),
        reps("lateral_footwork", "Lateral martial footwork", MovementFamily.MOVEMENT, 4, 8, perSide = true, cues = arrayOf("Do not cross the feet", "Push from the trailing leg", "Stay light")),
        reps("kick_chamber", "Slow kick chamber", MovementFamily.MOVEMENT, 5, 5, perSide = true, cues = arrayOf("Use support if balance limits technique", "Lift and retract under control", "Do not snap the knee")),
        reps("sprawl_recover", "Sprawl, recover, stance", MovementFamily.MOVEMENT, 6, 5, cues = arrayOf("Control the floor contact", "Recover to a stable stance", "Quality before speed")),

        // Mobility
        seconds("deep_squat_hold", "Deep squat hold", MovementFamily.MOBILITY, 1, 20, sets = 2, cues = arrayOf("Hold support if useful", "Relax into a pain-free depth", "Keep breathing")),
        reps("shoulder_cars", "Shoulder controlled circles", MovementFamily.MOBILITY, 1, 3, perSide = true, cues = arrayOf("Move slowly", "Keep ribs quiet", "Use the largest pain-free circle")),
        reps("ankle_rocks", "Ankle rocks", MovementFamily.MOBILITY, 1, 8, perSide = true, cues = arrayOf("Keep heel down", "Guide knee over toes", "No bouncing")),
        reps("thoracic_rotation", "Open-book rotation", MovementFamily.MOBILITY, 2, 5, perSide = true, cues = arrayOf("Let the upper back rotate", "Keep the movement easy and controlled")),
        reps("hip_9090", "90/90 hip switches", MovementFamily.MOBILITY, 2, 5, perSide = true, cues = arrayOf("Move from the hips", "Use hands as needed", "Do not force range")),
        seconds("active_hang", "Easy active hang", MovementFamily.MOBILITY, 2, 15, sets = 2, bar = true, cues = arrayOf("Use a secure bar", "Keep a little shoulder engagement", "Step down before grip fails")),
        reps("squat_to_stand", "Squat-to-stand flow", MovementFamily.MOBILITY, 3, 5, cues = arrayOf("Fold only as far as comfortable", "Sink into the squat", "Stand smoothly")),
        reps("cossack_flow", "Cossack mobility flow", MovementFamily.MOBILITY, 4, 4, perSide = true, cues = arrayOf("Stay controlled", "Use hands for support if useful")),

        // Conditioning
        reps("marching_jacks", "Low-impact jumping jack", MovementFamily.CONDITIONING, 1, 12, cues = arrayOf("Step instead of jump", "Keep breathing")),
        reps("mountain_climber", "Mountain climber", MovementFamily.CONDITIONING, 2, 10, perSide = true, cues = arrayOf("Hands stay planted", "Keep hips controlled", "Choose a sustainable pace")),
        reps("squat_thrust", "Squat thrust", MovementFamily.CONDITIONING, 3, 6, cues = arrayOf("Plant hands", "Step or hop feet back", "Return under control")),
        seconds("shadow_box", "Shadow-boxing interval", MovementFamily.CONDITIONING, 3, 30, sets = 3, cues = arrayOf("Stay relaxed", "Move the feet", "Punch into open space with control")),
        reps("burpee", "Burpee", MovementFamily.CONDITIONING, 4, 5, cues = arrayOf("Use a step-back version when useful", "Control floor contact", "Stand tall each rep")),
        seconds("fast_feet", "Fast-feet interval", MovementFamily.CONDITIONING, 5, 20, sets = 4, cues = arrayOf("Stay light", "Keep steps short", "Stop before form becomes sloppy")),
        seconds("crawl_interval", "Bear-crawl interval", MovementFamily.CONDITIONING, 6, 20, sets = 4, cues = arrayOf("Keep crawl mechanics clean", "Turn carefully", "Do not race poor positions")),
        reps("sprawl_interval", "Sprawl interval", MovementFamily.CONDITIONING, 7, 5, cues = arrayOf("Keep each floor contact controlled", "Recover to balance before the next rep")),
    )

    fun forFamily(family: MovementFamily): List<Exercise> = all.filter { it.family == family }
}
