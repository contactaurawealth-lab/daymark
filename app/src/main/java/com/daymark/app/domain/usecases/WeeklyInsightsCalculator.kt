package com.daymark.app.domain.usecases

import com.daymark.app.core.utilities.DateTimeUtils
import com.daymark.app.domain.models.Task
import com.daymark.app.domain.models.TimerSession
import java.time.LocalDate
import java.time.ZoneId

data class DayStat(
    val dayLabel: String,
    val completedCount: Int,
    val isToday: Boolean
)

data class WeeklyProductivityStats(
    val totalCompletedThisWeek: Int = 0,
    val totalFocusMinutesThisWeek: Int = 0,
    val dailyBreakdown: List<DayStat> = emptyList(),
    val mostProductiveDay: String = "—",
    val currentCompletionRate: Int = 0
)

object WeeklyInsightsCalculator {

    fun calculate(
        tasks: List<Task>,
        timerSessions: List<TimerSession> = emptyList(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): WeeklyProductivityStats {
        val today = LocalDate.now(zoneId)
        val past7Days = (6 downTo 0).map { today.minusDays(it.toLong()) }
        val startOfWindowMillis = past7Days.first().atStartOfDay(zoneId).toInstant().toEpochMilli()

        // Filter tasks completed in the 7 day window
        val completedInWindow = tasks.filter { task ->
            task.completed && (
                task.completedAt?.let { it >= startOfWindowMillis } ?: false
            )
        }

        val dayStats = past7Days.map { date ->
            val startMillis = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val endMillis = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

            val count = completedInWindow.count { task ->
                val time = task.completedAt ?: 0L
                time in startMillis until endMillis
            }

            DayStat(
                dayLabel = date.dayOfWeek.name.take(3), // e.g. MON, TUE
                completedCount = count,
                isToday = date == today
            )
        }

        val totalCompleted = dayStats.sumOf { it.completedCount }

        // Total focus minutes
        val totalFocusSeconds = timerSessions
            .filter { it.startedAt >= startOfWindowMillis }
            .sumOf { it.elapsedSeconds }
        val totalFocusMinutes = (totalFocusSeconds / 60).toInt()

        val bestDay = dayStats.maxByOrNull { it.completedCount }?.takeIf { it.completedCount > 0 }?.dayLabel ?: "—"

        return WeeklyProductivityStats(
            totalCompletedThisWeek = totalCompleted,
            totalFocusMinutesThisWeek = totalFocusMinutes,
            dailyBreakdown = dayStats,
            mostProductiveDay = bestDay
        )
    }
}
