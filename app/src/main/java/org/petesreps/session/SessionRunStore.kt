package org.petesreps.session

import android.content.Context
import java.nio.charset.StandardCharsets
import java.util.Base64

data class ActiveSessionRun(
    val dayNumber: Int,
    val sessionStartedElapsedMillis: Long,
    val blockStartedElapsedMillis: Long,
    val currentIndex: Int,
    val actuals: Map<String, Int>,
)

/**
 * Small local checkpoint for an in-progress session.
 *
 * This is intentionally separate from longitudinal training history: it exists
 * only so rotation, backgrounding, or process recreation cannot reset the
 * 25-minute clock or discard objective results already entered.
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
        )
    }

    fun save(run: ActiveSessionRun) {
        preferences.edit()
            .putInt(KEY_DAY, run.dayNumber)
            .putLong(KEY_SESSION_STARTED, run.sessionStartedElapsedMillis)
            .putLong(KEY_BLOCK_STARTED, run.blockStartedElapsedMillis)
            .putInt(KEY_INDEX, run.currentIndex.coerceAtLeast(0))
            .putString(KEY_ACTUALS, encodeActuals(run.actuals))
            .apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private fun encodeActuals(actuals: Map<String, Int>): String = actuals.entries
        .sortedBy { it.key }
        .joinToString("\n") { (exerciseId, value) ->
            val encodedId = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(exerciseId.toByteArray(StandardCharsets.UTF_8))
            "$encodedId:${value.coerceAtLeast(0)}"
        }

    private fun decodeActuals(encoded: String?): Map<String, Int> {
        if (encoded.isNullOrBlank()) return emptyMap()
        return encoded.lineSequence().mapNotNull { line ->
            val separator = line.indexOf(':')
            if (separator <= 0) return@mapNotNull null
            runCatching {
                val id = String(
                    Base64.getUrlDecoder().decode(line.substring(0, separator)),
                    StandardCharsets.UTF_8,
                )
                val value = line.substring(separator + 1).toInt().coerceAtLeast(0)
                id to value
            }.getOrNull()
        }.toMap()
    }

    companion object {
        private const val PREFS_NAME = "petes_reps_active_session"
        private const val KEY_DAY = "day"
        private const val KEY_SESSION_STARTED = "session_started_elapsed"
        private const val KEY_BLOCK_STARTED = "block_started_elapsed"
        private const val KEY_INDEX = "current_index"
        private const val KEY_ACTUALS = "actuals"
    }
}
