package org.petesreps.session

import android.content.Context
import java.nio.charset.StandardCharsets
import java.util.Base64

data class ActivePrescriptionState(
    val exerciseId: String,
    val sets: Int,
    val targetPerSet: Int,
    val blockMinutes: Int,
    val stimulusFamilyName: String,
)

data class ActiveSessionRun(
    val dayNumber: Int,
    val sessionStartedElapsedMillis: Long,
    val blockStartedElapsedMillis: Long,
    val currentIndex: Int,
    val actuals: Map<String, Int>,
    val prescriptions: List<ActivePrescriptionState> = emptyList(),
)

/**
 * Small local checkpoint for an in-progress session.
 *
 * This is intentionally separate from longitudinal training history: it exists
 * only so rotation, backgrounding, or process recreation cannot reset the
 * 25-minute clock, discard objective results already entered, or silently undo
 * a one-tap movement substitution.
 */
class SessionRunStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(dayNumber: Int): ActiveSessionRun? {
        if (preferences.getInt(KEY_DAY, -1) != dayNumber) return null
        val started = preferences.getLong(KEY_SESSION_STARTED, -1L)
        val blockStarted = preferences.getLong(KEY_BLOCK_STARTED, started)
        if (started < 0L || blockStarted < 0L) return null
        return ActiveSessionRun(
            dayNumber = dayNumber,
            sessionStartedElapsedMillis = started,
            blockStartedElapsedMillis = blockStarted,
            currentIndex = preferences.getInt(KEY_INDEX, 0).coerceAtLeast(0),
            actuals = decodeActuals(preferences.getString(KEY_ACTUALS, null)),
            prescriptions = decodePrescriptions(preferences.getString(KEY_PRESCRIPTIONS, null)),
        )
    }

    fun save(run: ActiveSessionRun) {
        preferences.edit()
            .putInt(KEY_DAY, run.dayNumber)
            .putLong(KEY_SESSION_STARTED, run.sessionStartedElapsedMillis)
            .putLong(KEY_BLOCK_STARTED, run.blockStartedElapsedMillis)
            .putInt(KEY_INDEX, run.currentIndex.coerceAtLeast(0))
            .putString(KEY_ACTUALS, encodeActuals(run.actuals))
            .putString(KEY_PRESCRIPTIONS, encodePrescriptions(run.prescriptions))
            .apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private fun encodeActuals(actuals: Map<String, Int>): String = actuals.entries
        .sortedBy { it.key }
        .joinToString("\n") { (exerciseId, value) ->
            val encodedId = encodeText(exerciseId)
            "$encodedId:${value.coerceAtLeast(0)}"
        }

    private fun decodeActuals(encoded: String?): Map<String, Int> {
        if (encoded.isNullOrBlank()) return emptyMap()
        return encoded.lineSequence().mapNotNull { line ->
            val separator = line.indexOf(':')
            if (separator <= 0) return@mapNotNull null
            runCatching {
                val id = decodeText(line.substring(0, separator))
                val value = line.substring(separator + 1).toInt().coerceAtLeast(0)
                id to value
            }.getOrNull()
        }.toMap()
    }

    private fun encodePrescriptions(items: List<ActivePrescriptionState>): String = items
        .joinToString("\n") { item ->
            listOf(
                encodeText(item.exerciseId),
                item.sets.coerceAtLeast(1).toString(),
                item.targetPerSet.coerceAtLeast(1).toString(),
                item.blockMinutes.coerceAtLeast(1).toString(),
                item.stimulusFamilyName,
            ).joinToString("|")
        }

    private fun decodePrescriptions(encoded: String?): List<ActivePrescriptionState> {
        if (encoded.isNullOrBlank()) return emptyList()
        return encoded.lineSequence().mapNotNull { line ->
            val parts = line.split('|')
            if (parts.size != 5) return@mapNotNull null
            runCatching {
                ActivePrescriptionState(
                    exerciseId = decodeText(parts[0]),
                    sets = parts[1].toInt().coerceAtLeast(1),
                    targetPerSet = parts[2].toInt().coerceAtLeast(1),
                    blockMinutes = parts[3].toInt().coerceAtLeast(1),
                    stimulusFamilyName = parts[4],
                )
            }.getOrNull()
        }.toList()
    }

    private fun encodeText(value: String): String = Base64.getUrlEncoder().withoutPadding()
        .encodeToString(value.toByteArray(StandardCharsets.UTF_8))

    private fun decodeText(value: String): String = String(
        Base64.getUrlDecoder().decode(value),
        StandardCharsets.UTF_8,
    )

    companion object {
        private const val PREFS_NAME = "petes_reps_active_session"
        private const val KEY_DAY = "day"
        private const val KEY_SESSION_STARTED = "session_started_elapsed"
        private const val KEY_BLOCK_STARTED = "block_started_elapsed"
        private const val KEY_INDEX = "current_index"
        private const val KEY_ACTUALS = "actuals"
        private const val KEY_PRESCRIPTIONS = "prescriptions"
    }
}
