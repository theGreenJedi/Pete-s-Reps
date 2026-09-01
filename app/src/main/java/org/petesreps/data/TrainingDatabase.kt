package org.petesreps.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.petesreps.model.MovementFamily
import org.petesreps.model.TrainingProfile
import org.petesreps.model.TrainingSummary
import org.petesreps.model.Workout

class TrainingDatabase(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE workouts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                day_number INTEGER NOT NULL,
                cycle_number INTEGER NOT NULL,
                cycle_day INTEGER NOT NULL,
                focus TEXT NOT NULL,
                planned_minutes INTEGER NOT NULL,
                completed_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE performances (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                workout_id INTEGER NOT NULL,
                exercise_id TEXT NOT NULL,
                family TEXT NOT NULL,
                prescribed_total INTEGER NOT NULL,
                actual_total INTEGER NOT NULL,
                unit TEXT NOT NULL,
                challenge INTEGER NOT NULL,
                completed_at INTEGER NOT NULL,
                FOREIGN KEY(workout_id) REFERENCES workouts(id)
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_performance_exercise ON performances(exercise_id)")
        db.execSQL("CREATE INDEX idx_performance_family ON performances(family)")
        db.execSQL("CREATE TABLE state (key TEXT PRIMARY KEY NOT NULL, value TEXT NOT NULL)")
        putState(db, "current_day", "1")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun profile(): TrainingProfile {
        val db = readableDatabase
        val families = MovementFamily.entries
        return TrainingProfile(
            dayNumber = intState(db, "current_day", 1),
            challengeByFamily = families.associateWith { intState(db, "challenge_${it.name}", 0) },
            successStreakByFamily = families.associateWith { intState(db, "success_${it.name}", 0) },
            failureStreakByFamily = families.associateWith { intState(db, "failure_${it.name}", 0) },
            lastExerciseByFamily = families.associateWith { stringState(db, "last_${it.name}") },
        )
    }

    fun summary(): TrainingSummary {
        val db = readableDatabase
        val workouts = db.rawQuery("SELECT COUNT(*) FROM workouts", null).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
        return TrainingSummary(
            currentDay = intState(db, "current_day", 1),
            workoutsLogged = workouts,
            challengeByFamily = MovementFamily.entries.associateWith { intState(db, "challenge_${it.name}", 0) },
        )
    }

    fun completeWorkout(workout: Workout, actualTotals: Map<String, Int>) {
        val db = writableDatabase
        val now = System.currentTimeMillis()
        db.beginTransaction()
        try {
            val workoutId = db.insertOrThrow("workouts", null, ContentValues().apply {
                put("day_number", workout.dayNumber)
                put("cycle_number", workout.cycleNumber)
                put("cycle_day", workout.cycleDay)
                put("focus", workout.focus)
                put("planned_minutes", workout.plannedMinutes)
                put("completed_at", now)
            })

            workout.prescriptions.forEach { prescription ->
                val family = prescription.exercise.family
                val actual = (actualTotals[prescription.exercise.id] ?: 0).coerceAtLeast(0)
                val prescribed = prescription.totalTarget.coerceAtLeast(1)
                val challengeKey = "challenge_${family.name}"
                val successKey = "success_${family.name}"
                val failureKey = "failure_${family.name}"
                var challenge = intState(db, challengeKey, 0)
                var success = intState(db, successKey, 0)
                var failure = intState(db, failureKey, 0)

                when {
                    actual >= prescribed -> {
                        success += 1
                        failure = 0
                        if (success >= 3) {
                            challenge += 1
                            success = 0
                        }
                    }
                    actual * 10 < prescribed * 7 -> {
                        failure += 1
                        success = 0
                        if (failure >= 2) {
                            challenge = (challenge - 1).coerceAtLeast(0)
                            failure = 0
                        }
                    }
                    else -> {
                        success = 0
                        failure = 0
                    }
                }

                db.insertOrThrow("performances", null, ContentValues().apply {
                    put("workout_id", workoutId)
                    put("exercise_id", prescription.exercise.id)
                    put("family", family.name)
                    put("prescribed_total", prescribed)
                    put("actual_total", actual)
                    put("unit", prescription.exercise.unit.name)
                    put("challenge", challenge)
                    put("completed_at", now)
                })

                putState(db, challengeKey, challenge.toString())
                putState(db, successKey, success.toString())
                putState(db, failureKey, failure.toString())
                putState(db, "last_${family.name}", prescription.exercise.id)
            }

            putState(db, "current_day", (workout.dayNumber + 1).toString())
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun intState(db: SQLiteDatabase, key: String, default: Int): Int =
        stringState(db, key)?.toIntOrNull() ?: default

    private fun stringState(db: SQLiteDatabase, key: String): String? =
        db.rawQuery("SELECT value FROM state WHERE key = ?", arrayOf(key)).use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }

    private fun putState(db: SQLiteDatabase, key: String, value: String) {
        db.insertWithOnConflict("state", null, ContentValues().apply {
            put("key", key)
            put("value", value)
        }, SQLiteDatabase.CONFLICT_REPLACE)
    }

    companion object {
        private const val DB_NAME = "petes_reps.db"
        private const val DB_VERSION = 1
    }
}
