package com.daymark.app

import com.daymark.app.domain.models.Task
import com.daymark.app.domain.models.TimerSession
import com.daymark.app.domain.models.TimerStatus
import com.daymark.app.domain.usecases.WeeklyInsightsCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class WeeklyInsightsCalculatorTest {

    private val zone = ZoneId.of("UTC")

    @Test
    fun calculate_emptyTasks_returnsZeroStats() {
        val stats = WeeklyInsightsCalculator.calculate(emptyList(), emptyList(), zone)
        assertEquals(0, stats.totalCompletedThisWeek)
        assertEquals(0, stats.totalFocusMinutesThisWeek)
        assertEquals(7, stats.dailyBreakdown.size)
        assertEquals("—", stats.mostProductiveDay)
    }

    @Test
    fun calculate_tasksCompletedWithinWindow_aggregatesProperly() {
        val today = LocalDate.now(zone)
        val todayMillis = today.atStartOfDay(zone).toInstant().toEpochMilli() + 3600000L

        val tasks = listOf(
            Task(id = 1, title = "Task 1", completed = true, completedAt = todayMillis),
            Task(id = 2, title = "Task 2", completed = true, completedAt = todayMillis + 1000L),
            Task(id = 3, title = "Task 3 (not completed)", completed = false, completedAt = null),
            Task(id = 4, title = "Task 4 (old)", completed = true, completedAt = todayMillis - (10 * 86400000L))
        )

        val sessions = listOf(
            TimerSession(id = 1, taskId = 1, durationSeconds = 1500, startedAt = todayMillis, elapsedSeconds = 1500, status = TimerStatus.COMPLETED),
            TimerSession(id = 2, taskId = 2, durationSeconds = 1500, startedAt = todayMillis, elapsedSeconds = 900, status = TimerStatus.PAUSED)
        )

        val stats = WeeklyInsightsCalculator.calculate(tasks, sessions, zone)
        assertEquals(2, stats.totalCompletedThisWeek)
        assertEquals(40, stats.totalFocusMinutesThisWeek) // (1500 + 900) / 60 = 40 minutes
        assertNotNull(stats.mostProductiveDay)
    }
}
