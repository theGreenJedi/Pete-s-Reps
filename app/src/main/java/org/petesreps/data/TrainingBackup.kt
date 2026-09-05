package org.petesreps.data

import java.nio.charset.StandardCharsets
import java.util.Base64

/**
 * Durable, app-owned backup representation of Pete's Reps training history.
 *
 * The on-disk format is deliberately independent of SQLite so a future database
 * migration does not make old exports unreadable. String fields are URL-safe
 * Base64 encoded, keeping the container line-oriented and deterministic.
 */
data class TrainingBackup(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val createdAtEpochMillis: Long,
    val state: List<BackupState>,
    val workouts: List<BackupWorkout>,
    val performances: List<BackupPerformance>,
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
    }
}

data class BackupState(
    val key: String,
    val value: String,
)

data class BackupWorkout(
    val id: Long,
    val dayNumber: Int,
    val cycleNumber: Int,
    val cycleDay: Int,
    val focus: String,
    val plannedMinutes: Int,
    val completedAt: Long,
)

data class BackupPerformance(
    val id: Long,
    val workoutId: Long,
    val exerciseId: String,
    val family: String,
    val prescribedTotal: Int,
    val actualTotal: Int,
    val unit: String,
    val challenge: Int,
    val completedAt: Long,
)

object TrainingBackupCodec {
    private const val MAGIC = "PETES_REPS_BACKUP"

    fun encode(backup: TrainingBackup): String = buildString {
        append(MAGIC)
        append('\t')
        append(backup.schemaVersion)
        append('\t')
        append(backup.createdAtEpochMillis)
        append('\n')

        backup.state.sortedBy { it.key }.forEach { item ->
            append("STATE\t${text(item.key)}\t${text(item.value)}\n")
        }
        backup.workouts.sortedBy { it.id }.forEach { item ->
            append(
                listOf(
                    "WORKOUT",
                    item.id,
                    item.dayNumber,
                    item.cycleNumber,
                    item.cycleDay,
                    text(item.focus),
                    item.plannedMinutes,
                    item.completedAt,
                ).joinToString("\t")
            )
            append('\n')
        }
        backup.performances.sortedBy { it.id }.forEach { item ->
            append(
                listOf(
                    "PERFORMANCE",
                    item.id,
                    item.workoutId,
                    text(item.exerciseId),
                    text(item.family),
                    item.prescribedTotal,
                    item.actualTotal,
                    text(item.unit),
                    item.challenge,
                    item.completedAt,
                ).joinToString("\t")
            )
            append('\n')
        }
    }

    fun decode(encoded: String): TrainingBackup {
        val lines = encoded.lineSequence().filter { it.isNotBlank() }.toList()
        require(lines.isNotEmpty()) { "Backup is empty." }

        val header = lines.first().split('\t')
        require(header.size == 3 && header[0] == MAGIC) { "Not a Pete's Reps backup." }
        val version = header[1].toIntOrNull() ?: error("Backup schema version is invalid.")
        require(version == TrainingBackup.CURRENT_SCHEMA_VERSION) {
            "Unsupported Pete's Reps backup schema $version."
        }
        val createdAt = header[2].toLongOrNull() ?: error("Backup creation time is invalid.")

        val state = mutableListOf<BackupState>()
        val workouts = mutableListOf<BackupWorkout>()
        val performances = mutableListOf<BackupPerformance>()

        lines.drop(1).forEachIndexed { index, line ->
            val lineNumber = index + 2
            val parts = line.split('\t')
            when (parts.firstOrNull()) {
                "STATE" -> {
                    require(parts.size == 3) { "Malformed STATE record at line $lineNumber." }
                    state += BackupState(untext(parts[1]), untext(parts[2]))
                }
                "WORKOUT" -> {
                    require(parts.size == 8) { "Malformed WORKOUT record at line $lineNumber." }
                    workouts += BackupWorkout(
                        id = long(parts[1], lineNumber, "workout id"),
                        dayNumber = int(parts[2], lineNumber, "day number"),
                        cycleNumber = int(parts[3], lineNumber, "training week"),
                        cycleDay = int(parts[4], lineNumber, "session in week"),
                        focus = untext(parts[5]),
                        plannedMinutes = int(parts[6], lineNumber, "planned minutes"),
                        completedAt = long(parts[7], lineNumber, "completion time"),
                    )
                }
                "PERFORMANCE" -> {
                    require(parts.size == 10) { "Malformed PERFORMANCE record at line $lineNumber." }
                    performances += BackupPerformance(
                        id = long(parts[1], lineNumber, "performance id"),
                        workoutId = long(parts[2], lineNumber, "workout id"),
                        exerciseId = untext(parts[3]),
                        family = untext(parts[4]),
                        prescribedTotal = int(parts[5], lineNumber, "prescribed total"),
                        actualTotal = int(parts[6], lineNumber, "actual total"),
                        unit = untext(parts[7]),
                        challenge = int(parts[8], lineNumber, "challenge"),
                        completedAt = long(parts[9], lineNumber, "completion time"),
                    )
                }
                else -> error("Unknown backup record at line $lineNumber.")
            }
        }

        return TrainingBackup(
            schemaVersion = version,
            createdAtEpochMillis = createdAt,
            state = state,
            workouts = workouts,
            performances = performances,
        )
    }

    private fun text(value: String): String = Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(value.toByteArray(StandardCharsets.UTF_8))

    private fun untext(value: String): String = try {
        String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8)
    } catch (error: IllegalArgumentException) {
        throw IllegalArgumentException("Backup contains invalid encoded text.", error)
    }

    private fun int(value: String, line: Int, field: String): Int =
        value.toIntOrNull() ?: error("Invalid $field at line $line.")

    private fun long(value: String, line: Int, field: String): Long =
        value.toLongOrNull() ?: error("Invalid $field at line $line.")
}
