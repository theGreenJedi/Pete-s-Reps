package org.petesreps.data

import org.petesreps.model.Exercise

/**
 * Plain-language setup and movement descriptions for every exercise in the v0 catalog.
 * These are deliberately short enough to read during a 25-minute session. Technique
 * cues remain on Exercise so the two layers can evolve independently.
 */
object ExerciseGuides {
    private val descriptions: Map<String, String> = mapOf(
        "wall_pushup" to "Stand facing a wall with your hands around shoulder height. Keep your body in one line, bend your elbows to bring your chest toward the wall, then press back to standing.",
        "incline_pushup" to "Place your hands on a sturdy raised surface and walk your feet back until your body is straight. Lower your chest toward the edge, then press back up.",
        "knee_pushup" to "Start face-down supported on your hands and knees, with a straight line from knees through shoulders. Lower your chest toward the floor and press back up.",
        "pushup" to "Start in a high plank with hands around shoulder width. Lower chest and hips together toward the floor, then press back to straight arms.",
        "pause_pushup" to "Perform a normal push-up, but stop briefly just above the floor before pressing back up. Keep your whole body braced during the pause.",
        "decline_pushup" to "Put your feet on a low stable surface and your hands on the floor in a push-up position. Lower under control and press back up without letting the hips sag.",
        "diamond_pushup" to "Use a narrower-than-normal hand position under your chest. Keep your body straight, lower under control with elbows close, and press back up.",
        "pike_pushup" to "Start with hands and feet on the floor and hips high so your body forms an inverted V. Bend your elbows and lower your head forward and down between your hands, then press back up.",
        "archer_pushup" to "Take a wide push-up stance. Shift toward one arm as you bend that elbow while the other arm stays mostly straight, then press back to center and repeat on the other side.",
        "wall_hspu_negative" to "Set up in a wall-supported handstand. With control, bend your elbows and slowly lower your head toward the floor through only the range you can own, then come down and reset.",

        "dead_hang" to "Grip a secure pull-up bar and let your body hang beneath it with long arms and feet clear of the floor. Hold the position without swinging, then step down before your grip gives out.",
        "scap_pull" to "Hang from a secure bar with straight elbows. Without bending your arms, pull your shoulders down away from your ears to lift your body slightly, then relax back to the start.",
        "towel_hang" to "Drape a strong towel over a secure pull-up bar and grip one end in each hand. Lift your feet and hang from the towel while keeping the grip even, then step down under control.",
        "flexed_hang" to "Use a step or support to begin with your chin near or above the pull-up bar and elbows bent. Hold that position, then lower or step down under control.",
        "negative_pullup" to "Use a step to begin with your chin above the bar. Remove your support and lower yourself slowly until your arms are straight, then step back up to reset.",
        "chinup" to "Hang from the bar with palms facing you. Pull by driving your elbows down until your chin clears the bar, then lower to a controlled hang.",
        "pullup" to "Hang from the bar with palms facing away. Brace, pull your chest upward until your chin clears the bar, and lower under control without swinging.",
        "pause_pullup" to "Perform a pull-up and pause briefly near the top before lowering. Keep the pause still rather than using momentum or a kip.",
        "chest_to_bar" to "Start from a controlled hang and pull higher than a normal pull-up, bringing the upper chest toward the bar. Lower smoothly without jerking or swinging.",

        "chair_squat" to "Stand just in front of a sturdy chair with feet in a comfortable squat stance. Sit your hips back until you lightly touch the chair, then stand without rocking for momentum.",
        "bodyweight_squat" to "Stand with feet at a comfortable width. Sit hips down and back while knees track with toes, reach the depth you can control, then stand tall.",
        "reverse_lunge" to "Stand tall, step one foot backward, and lower both knees under control. Push through the front foot to return to standing, then repeat on the other side.",
        "split_squat" to "Take a staggered stance and keep both feet planted. Lower your body mostly straight down between your feet, then drive through the front leg to rise.",
        "kb_goblet_squat" to "Hold a kettlebell close to your chest with both hands. Squat to a controlled depth while keeping the bell close, then stand tall.",
        "lateral_lunge" to "Step or shift out to one side and sit your hips back over that leg while the other leg stays long. Push through the bent leg to return to center.",
        "cossack_squat" to "Take a wide stance and shift deeply toward one leg while the other leg straightens. Keep the working foot planted and move only through the range you can control.",
        "bulgarian_split" to "Stand in a split stance with the rear foot resting on a low stable support. Lower through the front leg, keeping most pressure on that foot, then stand back up.",
        "assisted_pistol" to "Stand on one leg while holding a stable support. Reach the other leg forward, lower into a one-leg squat using the support as needed, then stand back up under control.",

        "dead_bug" to "Lie on your back with arms pointed at the ceiling and hips and knees bent about 90 degrees. Keep your lower back controlled as you slowly reach one arm overhead and extend the opposite leg, return to center, then switch sides.",
        "front_plank" to "Support yourself on forearms and toes with your body in one straight line. Brace your trunk and glutes, keep the hips from sagging or hiking, and breathe while you hold.",
        "side_plank" to "Lie on one side and support yourself on one forearm beneath the shoulder. Lift your hips so your body forms a straight line, hold, then repeat on the other side.",
        "hollow_rock" to "Lie on your back and lift shoulders and legs slightly while pressing the low back toward the floor. Keep that curved hollow shape and make small controlled rocks without changing position.",
        "tuck_lsit" to "Sit between your hands on the floor or stable handles. Press down through the hands, lift your hips and bent knees off the floor, and hold the tucked position without shrugging.",
        "hanging_knee_raise" to "Hang quietly from a secure bar. Curl your knees upward toward your ribs without swinging, lower them under control, and reset before the next rep.",
        "hanging_leg_raise" to "Hang from the bar with your body quiet. Keeping the legs relatively straight, raise them as high as you can without swinging, then lower under control.",
        "toes_toward_bar" to "Hang from a secure bar and raise your straight or slightly bent legs toward the bar using trunk control. Lower slowly and avoid using a swing to reach higher.",

        "technical_standup" to "Sit with one hand posted behind you and the opposite foot planted. Lift your hips, sweep the free leg back underneath you while maintaining space, and rise into a stable stance; reverse to reset.",
        "bear_steps" to "Start on hands and feet with knees hovering just above the floor. Crawl by moving one hand with the opposite foot, taking small quiet steps while keeping your back steady.",
        "shrimp" to "Lie on your back or side with knees bent. Bridge lightly, turn to one side, and use your feet and shoulders to slide your hips away, then reset and repeat the other direction.",
        "crab_steps" to "Sit with feet and hands on the floor, fingers in a comfortable direction, then lift your hips. Move by taking short coordinated steps with hands and feet while keeping the shoulders comfortable.",
        "stance_switch" to "Start in a balanced fighting stance. Switch which foot is forward with a small controlled hop or step, land quietly in balance, and repeat without crossing your feet.",
        "kb_suitcase_carry" to "Hold a kettlebell at one side like a suitcase. Stand tall without leaning, then walk under control for the prescribed time before switching hands.",
        "sprawl_to_stand" to "From standing, place your hands on the floor and send or step your feet back into a controlled sprawl. Bring the feet back underneath you and stand into balance.",
        "lateral_footwork" to "Begin in a balanced athletic or fighting stance. Move sideways by pushing from the trailing leg and stepping with the lead foot first, keeping the feet from crossing.",
        "wall_handstand" to "Use a clear wall to support a handstand with hands planted securely on the floor. Push tall through your shoulders, hold a controlled body position, and come down before the position degrades.",
        "kick_chamber" to "Stand on one leg, using light support if needed. Slowly lift the other knee into a controlled kicking chamber, extend only if prescribed, retract it, and place the foot down with balance.",
        "handstand_weight_shift" to "Set up in a wall-supported handstand. Shift your weight slowly toward one hand just enough to make the other hand light, return to center, and alternate sides.",
        "sprawl_recover" to "Drop into a controlled sprawl with hands contacting the floor before the feet travel back. Recover your feet underneath you and finish in a stable fighting stance before repeating.",

        "deep_squat_hold" to "Take a comfortable squat stance and sink into the deepest pain-free squat you can control. Use a stable support if helpful, keep your feet planted, and breathe while you hold.",
        "shoulder_cars" to "Stand or kneel tall with the rest of your body quiet. Move one straight arm slowly through the largest comfortable circle you can make, then repeat on the other side.",
        "ankle_rocks" to "Stand facing a wall or support with one foot planted. Keeping the heel down, guide that knee forward over the toes, then rock back and repeat before switching sides.",
        "half_kneeling_hip_flexor" to "Kneel with one knee down and the other foot forward. Gently tuck your pelvis, stay tall, and shift forward slightly until you feel a mild stretch at the front of the down-knee hip.",
        "hamstring_stretch" to "Place one leg in front of you with the knee straight or softly bent. Keep your spine long and hinge forward from the hip until you feel a comfortable stretch behind the thigh.",
        "wrist_extension_stretch" to "Extend one arm with the elbow comfortable and palm facing away or down. Use the other hand or the floor to apply light pressure that stretches the wrist and forearm without forcing it.",
        "thoracic_rotation" to "Lie on your side with hips and knees bent and arms together in front. Keep the knees stacked as you rotate the top arm and upper back open toward the other side, then return.",
        "hip_9090" to "Sit with both knees bent and feet on the floor. Let both knees rotate together from one side to the other, aiming for a 90/90 shape at each side while using your hands as needed.",
        "active_hang" to "Hang from a secure pull-up bar with straight arms while keeping a small amount of shoulder engagement instead of fully relaxing. Breathe and step down before grip or shoulder position degrades.",
        "squat_to_stand" to "From standing, hinge forward and reach toward your feet or shins, then lower your hips into a squat. Lift the chest, settle briefly, and stand smoothly to finish the rep.",
        "cossack_flow" to "Take a wide stance and shift from side to side into alternating lateral squat positions. Use your hands for support if needed and keep the motion smooth rather than forcing depth.",

        "marching_jacks" to "Stand tall and perform a low-impact jumping-jack pattern by stepping one foot out as the arms rise, returning to center, then alternating sides.",
        "mountain_climber" to "Start in a high plank. Bring one knee toward your chest, return it, and alternate legs while keeping your hands planted and hips controlled.",
        "squat_thrust" to "From standing, squat and place your hands on the floor. Step or hop both feet back to a plank, bring them forward again, and stand under control.",
        "shadow_box" to "Stand in a balanced fighting stance with open space around you. Move your feet and throw relaxed controlled punches into empty space for the prescribed interval.",
        "burpee" to "From standing, place your hands down and step or hop the feet back, lower only if your version calls for it, bring the feet forward again, and stand tall to complete the rep.",
        "kb_swing" to "Stand over a kettlebell, hike it back between your legs, then drive your hips forward to swing it. Let the bell float from hip power rather than lifting it with your arms, and guide it back into the next hinge.",
        "medicine_ball_slam" to "Hold a slam-safe medicine ball, reach it overhead, then drive it forcefully toward an appropriate floor using your whole body. Reset your stance and retrieve the ball before the next rep.",
        "fast_feet" to "Take an athletic stance and make very short quick alternating steps in place. Stay light and balanced rather than letting speed pull you out of position.",
        "crawl_interval" to "Use the same bear-crawl position: hands and feet down, knees hovering close to the floor. Crawl continuously with opposite hand and foot until the interval ends, turning carefully when needed.",
        "sprawl_interval" to "Repeat controlled sprawls for the interval: hands down first, feet back, then recover underneath yourself and regain balance before starting the next rep.",
    )

    fun descriptionFor(exercise: Exercise): String =
        descriptions[exercise.id]
            ?: error("Missing movement guide for exercise '${exercise.id}' (${exercise.name})")

    fun hasGuide(exerciseId: String): Boolean = descriptions.containsKey(exerciseId)
}
