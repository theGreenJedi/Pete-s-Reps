package org.petesreps.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.petesreps.model.MovementFamily
import org.petesreps.model.TrainingProfile

class WorkoutEngineTest {
    private val engine = WorkoutEngine()

    @Test
    fun everyDayStaysAtOrUnderTwentyFiveMinutes() {
        for (day in 1..400) {
            val workout = engine.generate(profile(day = day))
            assertTrue("Day $day was ${workout.plannedMinutes} minutes", workout.plannedMinutes <= 25)
        }
    }

    @Test
    fun cyclesRollForeverInsteadOfEnding() {
        assertEquals(1, engine.generate(profile(day = 56)).cycleNumber)
        assertEquals(2, engine.generate(profile(day = 57)).cycleNumber)
        assertEquals(6, engine.generate(profile(day = 300)).cycleNumber)
    }

    @Test
    fun higherChallengeMovesBeyondBeginnerPushWork() {
        val beginner = engine.generate(profile(day = 1, pushChallenge = 0))
            .prescriptions.first { it.exercise.family == MovementFamily.PUSH }
        val progressed = engine.generate(profile(day = 1, pushChallenge = 18))
            .prescriptions.first { it.exercise.family == MovementFamily.PUSH }

        assertTrue(progressed.exercise.tier > beginner.exercise.tier)
    }

    private fun profile(day: Int, pushChallenge: Int = 0): TrainingProfile {
        val families = MovementFamily.entries
        return TrainingProfile(
            dayNumber = day,
            challengeByFamily = families.associateWith { if (it == MovementFamily.PUSH) pushChallenge else 0 },
            successStreakByFamily = families.associateWith { 0 },
            failureStreakByFamily = families.associateWith { 0 },
            lastExerciseByFamily = families.associateWith { null },
        )
    }
}
