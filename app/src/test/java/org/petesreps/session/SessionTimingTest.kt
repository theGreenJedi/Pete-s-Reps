package org.petesreps.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionTimingTest {
    @Test
    fun sessionStartsAtExactlyTwentyFiveMinutes() {
        assertEquals(
            SessionTiming.SESSION_LIMIT_MILLIS,
            SessionTiming.remainingMillis(startedAtElapsedMillis = 1_000L, nowElapsedMillis = 1_000L),
        )
        assertEquals("25:00", SessionTiming.formatClock(SessionTiming.SESSION_LIMIT_MILLIS))
    }

    @Test
    fun remainingTimeUsesElapsedTimestampInsteadOfTickCount() {
        val start = 10_000L
        val now = start + 7L * 60L * 1000L + 12_345L
        assertEquals(
            17L * 60L * 1000L + 47_655L,
            SessionTiming.remainingMillis(start, now),
        )
    }

    @Test
    fun sessionHardStopsAtTwentyFiveMinutesAndNeverGoesNegative() {
        val start = 50_000L
        assertFalse(SessionTiming.isExpired(start, start + SessionTiming.SESSION_LIMIT_MILLIS - 1L))
        assertTrue(SessionTiming.isExpired(start, start + SessionTiming.SESSION_LIMIT_MILLIS))
        assertTrue(SessionTiming.isExpired(start, start + SessionTiming.SESSION_LIMIT_MILLIS + 30_000L))
        assertEquals(0L, SessionTiming.remainingMillis(start, start + SessionTiming.SESSION_LIMIT_MILLIS + 30_000L))
        assertEquals("00:00", SessionTiming.formatClock(0L))
    }

    @Test
    fun blockTimerIsSubordinateToItsOwnEntryTimestamp() {
        val blockStart = 100_000L
        assertEquals(
            2L * 60L * 1000L,
            SessionTiming.blockRemainingMillis(blockStart, blockStart + 3L * 60L * 1000L, blockMinutes = 5),
        )
        assertEquals(
            0L,
            SessionTiming.blockRemainingMillis(blockStart, blockStart + 6L * 60L * 1000L, blockMinutes = 5),
        )
    }

    @Test
    fun persistedTimestampFromPreviousBootFailsClosed() {
        assertTrue(SessionTiming.isExpired(startedAtElapsedMillis = 500_000L, nowElapsedMillis = 20_000L))
    }
}
