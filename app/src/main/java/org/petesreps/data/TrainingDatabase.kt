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
            underperformanceStreakByFamily = families.associateWith {
                intState(db, "under_${it.name}", intState(db, "failure_${it.name}", 0))
            },
            lastExerciseByFamily = families.associateWith { stringState(db, "last_${it.name}") },
            lastTrainedDayByFamily = families.associateWith { intState(db, "last_trained_${it.name}", 0) },
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
                val family = prescription.stimulusFamily
                val actual = (actualTotals[prescription.exercise.id] ?: 0).coerceAtLeast(0)
                val prescribed = prescription.totalTarget.coerceAtLeast(1)
                val challengeKey = "challenge_${family.name}"
                val successKey = "success_${family.name}"
                val underKey = "under_${family.name}"
                var challenge = intState(db, challengeKey, 0)
                var success = intState(db, successKey, 0)
                var underperformance = intState(db, underKey, intState(db, "failure_${family.name}", 0))

                when {
                    actual >= prescribed -> {
                        success += 1
                        underperformance = 0
                        if (success >= 3) {
                            challenge += 1
                            success = 0
                        }
                    }
                    actual * 10 < prescribed * 7 -> {
                        underperformance += 1
                        success = 0
                        if (underperformance >= 2) {
                            challenge = (challenge - 1).coerceAtLeast(0)
                            underperformance = 0
                        }
                    }
                    else -> {
                        success = 0
                        underperformance = 0
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
                putState(db, underKey, underperformance.toString())
                putState(db, "last_${family.name}", prescription.exercise.id)
                putState(db, "last_trained_${family.name}", workout.dayNumber.toString())
            }

            putState(db, "current_day", (workout.dayNumber + 1).toString())
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun backup(): TrainingBackup {
        val db = readableDatabase
        val state = db.rawQuery("SELECT key, value FROM state ORDER BY key", null).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(BackupState(cursor.getString(0), cursor.getString(1)))
            }
        }
        val workouts = db.rawQuery(
            "SELECT id, day_number, cycle_number, cycle_day, focus, planned_minutes, completed_at FROM workouts ORDER BY id",
            null,
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        BackupWorkout(
                            id = cursor.getLong(0),
                            dayNumber = cursor.getInt(1),
                            cycleNumber = cursor.getInt(2),
                            cycleDay = cursor.getInt(3),
                            focus = cursor.getString(4),
                            plannedMinutes = cursor.getInt(5),
                            completedAt = cursor.getLong(6),
                        )
                    )
                }
            }
        }
        val performances = db.rawQuery(
            "SELECT id, workout_id, exercise_id, family, prescribed_total, actual_total, unit, challenge, completed_at FROM performances ORDER BY id",
            null,
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        BackupPerformance(
                            id = cursor.getLong(0),
                            workoutId = cursor.getLong(1),
                            exerciseId = cursor.getString(2),
                            family = cursor.getString(3),
                            prescribedTotal = cursor.getInt(4),
                            actualTotal = cursor.getInt(5),
                            unit = cursor.getString(6),
                            challenge = cursor.getInt(7),
                            completedAt = cursor.getLong(8),
                        )
                    )
                }
            }
        }
        return TrainingBackup(
            createdAtEpochMillis = System.currentTimeMillis(),
            state = state,
            workouts = workouts,
            performances = performances,
        )
    }

    fun restoreBackup(backup: TrainingBackup) {
        validateBackup(backup)
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("performances", null, null)
            db.delete("workouts", null, null)
            db.delete("state", null, null)

            backup.workouts.sortedBy { it.id }.forEach { item ->
                db.insertOrThrow("workouts", null, ContentValues().apply {
                    put("id", item.id)
                    put("day_number", item.dayNumber)
                    put("cycle_number", item.cycleNumber)
                    put("cycle_day", item.cycleDay)
                    put("focus", item.focus)
                    put("planned_minutes", item.plannedMinutes)
                    put("completed_at", item.completedAt)
                })
            }
            backup.performances.sortedBy { it.id }.forEach { item ->
                db.insertOrThrow("performances", null, ContentValues().apply {
                    put("id", item.id)
                    put("workout_id", item.workoutId)
                    put("exercise_id", item.exerciseId)
                    put("family", item.family)
                    put("prescribed_total", item.prescribedTotal)
                    put("actual_total", item.actualTotal)
                    put("unit", item.unit)
                    put("challenge", item.challenge)
                    put("completed_at", item.completedAt)
                })
            }
            backup.state.forEach { item -> putState(db, item.key, item.value) }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun validateBackup(backup: TrainingBackup) {
        require(backup.schemaVersion == TrainingBackup.CURRENT_SCHEMA_VERSION) {
            "Unsupported backup schema ${backup.schemaVersion}."
        }
        require(backup.createdAtEpochMillis >= 0) { "Backup creation time is invalid." }

        val stateKeys = backup.state.map { it.key }
        require(stateKeys.size == stateKeys.toSet().size) { "Backup contains duplicate state keys." }
        val currentDay = backup.state.firstOrNull { it.key == "current_day" }?.value?.toIntOrNull()
        require(currentDay != null && currentDay >= 1) { "Backup has no valid current training session." }

        val workoutIds = backup.workouts.map { it.id }
        require(workoutIds.size == workoutIds.toSet().size) { "Backup contains duplicate workout ids." }
        backup.workouts.forEach { item ->
            require(item.id > 0) { "Backup contains an invalid workout id." }
            require(item.dayNumber >= 1) { "Backup contains an invalid session number." }
            require(item.cycleNumber >= 1 && item.cycleDay >= 1) { "Backup contains invalid legacy cycle metadata." }
            require(item.plannedMinutes in 0..25) { "Backup contains a workout above the 25-minute ceiling." }
            require(item.completedAt >= 0) { "Backup contains an invalid workout timestamp." }
        }

        val performanceIds = backup.performances.map { it.id }
        require(performanceIds.size == performanceIds.toSet().size) { "Backup contains duplicate performance ids." }
        val workoutIdSet = workoutIds.toSet()
        backup.performances.forEach { item ->
            require(item.id > 0) { "Backup contains an invalid performance id." }
            require(item.workoutId in workoutIdSet) { "Backup contains a performance without its workout." }
            require(item.exerciseId.isNotBlank()) { "Backup contains a performance without an exercise id." }
            require(item.family.isNotBlank() && item.unit.isNotBlank()) { "Backup contains incomplete performance metadata." }
            require(item.prescribedTotal >= 0 && item.actualTotal >= 0 && item.challenge >= 0) {
                "Backup contains a negative performance value."
            }
            require(item.completedAt >= 0) { "Backup contains an invalid performance timestamp." }
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
