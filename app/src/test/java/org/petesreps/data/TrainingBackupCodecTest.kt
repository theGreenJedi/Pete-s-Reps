package org.petesreps.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingBackupCodecTest {
    @Test
    fun roundTripPreservesAllBackupData() {
        val backup = TrainingBackup(
            createdAtEpochMillis = 123456789L,
            state = listOf(
                BackupState("current_day", "42"),
                BackupState("last_PULL", "towel\thang / café"),
            ),
            workouts = listOf(
                BackupWorkout(
                    id = 7,
                    dayNumber = 41,
                    cycleNumber = 2,
                    cycleDay = 17, // legacy 56-day metadata must remain representable
                    focus = "Grip + carry / mobility",
                    plannedMinutes = 23,
                    completedAt = 2222L,
                )
            ),
            performances = listOf(
                BackupPerformance(
                    id = 9,
                    workoutId = 7,
                    exerciseId = "towel_hang",
                    family = "PULL",
                    prescribedTotal = 45,
                    actualTotal = 52,
                    unit = "SECONDS",
                    challenge = 4,
                    completedAt = 2233L,
                )
            ),
        )

        val encoded = TrainingBackupCodec.encode(backup)
        val decoded = TrainingBackupCodec.decode(encoded)

        assertEquals(backup, decoded)
        assertTrue(encoded.startsWith("PETES_REPS_BACKUP\t1\t"))
    }

    @Test
    fun rejectsWrongMagic() {
        val failure = runCatching {
            TrainingBackupCodec.decode("SOMETHING_ELSE\t1\t123\n")
        }.exceptionOrNull()

        assertTrue(failure is IllegalArgumentException)
    }

    @Test
    fun rejectsUnsupportedSchema() {
        val failure = runCatching {
            TrainingBackupCodec.decode("PETES_REPS_BACKUP\t99\t123\n")
        }.exceptionOrNull()

        assertTrue(failure is IllegalArgumentException)
        assertTrue(failure?.message.orEmpty().contains("Unsupported"))
    }

    @Test
    fun rejectsMalformedRecord() {
        val failure = runCatching {
            TrainingBackupCodec.decode("PETES_REPS_BACKUP\t1\t123\nSTATE\tonly-one-field\n")
        }.exceptionOrNull()

        assertTrue(failure is IllegalArgumentException)
    }
}
