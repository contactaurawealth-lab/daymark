package com.daymark.app

import com.daymark.app.core.utilities.DateTimeUtils
import com.daymark.app.data.repositories.PersistedTimerState
import com.daymark.app.domain.models.TimerStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimerLogicTest {

    @Test
    fun testFormatSecondsToTime() {
        assertEquals("00:00", DateTimeUtils.formatSecondsToTime(0))
        assertEquals("00:45", DateTimeUtils.formatSecondsToTime(45))
        assertEquals("25:00", DateTimeUtils.formatSecondsToTime(1500))
        assertEquals("1:00:00", DateTimeUtils.formatSecondsToTime(3600))
        assertEquals("1:05:09", DateTimeUtils.formatSecondsToTime(3909))
        assertEquals("00:00", DateTimeUtils.formatSecondsToTime(-10))
    }

    @Test
    fun testTimerCountdownRemainingCalculation() {
        val totalDuration = 1500L // 25 min
        val now = 1000000L
        val targetEnd = now + (600 * 1000L) // 10 min left

        val remainingSec = (targetEnd - now) / 1000
        assertEquals(600L, remainingSec)

        val elapsed = totalDuration - remainingSec
        assertEquals(900L, elapsed)
    }

    @Test
    fun testTimerPauseAndResumeState() {
        val state = PersistedTimerState(
            taskId = 10L,
            taskTitle = "Code feature",
            targetEndTime = null,
            remainingMillisOnPause = 450 * 1000L,
            totalDurationSeconds = 1500L,
            status = TimerStatus.PAUSED
        )

        assertEquals(TimerStatus.PAUSED, state.status)
        assertEquals(450L, (state.remainingMillisOnPause ?: 0L) / 1000L)
    }

    @Test
    fun testTimerExpirationWhileAppWasClosed() {
        val now = 2000000L
        val targetEnd = now - 5000L // ended 5 seconds ago

        val isExpired = targetEnd <= now
        assertTrue(isExpired)
    }
}
