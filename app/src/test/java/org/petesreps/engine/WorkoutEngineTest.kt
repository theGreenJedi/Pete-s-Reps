package org.petesreps.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.petesreps.model.MovementFamily
import org.petesreps.model.TrainingProfile

class WorkoutEngineTest {
    private val engine = WorkoutEngine()

    @Test
    fun everySessionStaysAtOrUnderTwentyFiveMinutes() {
        for (day in 1..400) {
            val workout = engine.generate(profile(day = day))
            assertTrue("Session $day was ${workout.plannedMinutes} minutes", workout.plannedMinutes <= 25)
        }
    }

    @Test
    fun rollingTrainingRhythmUsesSixSessions() {
        assertEquals(1, engine.generate(profile(day = 6)).cycleNumber)
        assertEquals(6, engine.generate(profile(day = 6)).cycleDay)
        assertEquals(2, engine.generate(profile(day = 7)).cycleNumber)
        assertEquals(1, engine.generate(profile(day = 7)).cycleDay)
        assertEquals(50, engine.generate(profile(day = 300)).cycleNumber)
    }

    @Test
    fun mobilityAndStretchingRemainInEverySession() {
        for (day in 1..30) {
            val workout = engine.generate(profile(day = day))
            assertTrue(workout.prescriptions.any { it.exercise.family == MovementFamily.MOBILITY })
        }
    }

    @Test
    fun higherChallengeMovesBeyondBeginnerPushWork() {
        val exposure = MovementFamily.entries.associateWith { family ->
            if (family == MovementFamily.PUSH || family == MovementFamily.MOBILITY) 0 else 20
        }
        val beginner = engine.generate(profile(day = 20, pushChallenge = 0, lastTrained = exposure))
            .prescriptions.first { it.exercise.family == MovementFamily.PUSH }
        val progressed = engine.generate(profile(day = 20, pushChallenge = 18, lastTrained = exposure))
            .prescriptions.first { it.exercise.family == MovementFamily.PUSH }

        assertTrue(progressed.exercise.tier > beginner.exercise.tier)
    }

    @Test
    fun inferredUnderperformanceQuietlyReducesNextTarget() {
        val exposure = MovementFamily.entries.associateWith { family ->
            if (family == MovementFamily.PUSH || family == MovementFamily.MOBILITY) 0 else 20
        }
        val normal = engine.generate(profile(day = 20, pushChallenge = 6, lastTrained = exposure))
            .prescriptions.first { it.exercise.family == MovementFamily.PUSH }
        val reduced = engine.generate(
            profile(
                day = 20,
                pushChallenge = 6,
                lastTrained = exposure,
                underperformingFamily = MovementFamily.PUSH,
            ),
        ).prescriptions.first { it.exercise.family == MovementFamily.PUSH }

        assertTrue(reduced.targetPerSet < normal.targetPerSet)
    }

    private fun profile(
        day: Int,
        pushChallenge: Int = 0,
        lastTrained: Map<MovementFamily, Int> = emptyMap(),
        underperformingFamily: MovementFamily? = null,
    ): TrainingProfile {
        val families = MovementFamily.entries
        return TrainingProfile(
            dayNumber = day,
            challengeByFamily = families.associateWith { if (it == MovementFamily.PUSH) pushChallenge else 0 },
            successStreakByFamily = families.associateWith { 0 },
            underperformanceStreakByFamily = families.associateWith { if (it == underperformingFamily) 1 else 0 },
            lastExerciseByFamily = families.associateWith { null },
            lastTrainedDayByFamily = families.associateWith { lastTrained[it] ?: 0 },
        )
    }
}
