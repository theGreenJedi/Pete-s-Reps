package org.petesreps.session

/**
 * Authoritative timing math for a Pete's Reps session.
 *
 * The UI supplies Android's monotonic elapsed-realtime timestamps. Keeping the
 * arithmetic here Android-free makes the 25-minute contract deterministic and
 * unit-testable.
 */
object SessionTiming {
    const val SESSION_LIMIT_MILLIS: Long = 25L * 60L * 1000L

    fun elapsedMillis(startedAtElapsedMillis: Long, nowElapsedMillis: Long): Long {
        if (nowElapsedMillis < startedAtElapsedMillis) return SESSION_LIMIT_MILLIS
        return nowElapsedMillis - startedAtElapsedMillis
    }

    fun remainingMillis(
        startedAtElapsedMillis: Long,
        nowElapsedMillis: Long,
        limitMillis: Long = SESSION_LIMIT_MILLIS,
    ): Long = (limitMillis - elapsedMillis(startedAtElapsedMillis, nowElapsedMillis)).coerceAtLeast(0L)

    fun blockRemainingMillis(
        blockStartedAtElapsedMillis: Long,
        nowElapsedMillis: Long,
        blockMinutes: Int,
    ): Long = remainingMillis(
        startedAtElapsedMillis = blockStartedAtElapsedMillis,
        nowElapsedMillis = nowElapsedMillis,
        limitMillis = blockMinutes.coerceAtLeast(0) * 60_000L,
    )

    fun isExpired(startedAtElapsedMillis: Long, nowElapsedMillis: Long): Boolean =
        remainingMillis(startedAtElapsedMillis, nowElapsedMillis) == 0L

    fun displaySeconds(remainingMillis: Long): Long =
        if (remainingMillis <= 0L) 0L else (remainingMillis + 999L) / 1000L

    fun formatClock(remainingMillis: Long): String {
        val totalSeconds = displaySeconds(remainingMillis)
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        return "%02d:%02d".format(minutes, seconds)
    }
}
