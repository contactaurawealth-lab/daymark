package com.daymark.app.domain.usecases

import com.daymark.app.core.utilities.DateTimeUtils
import com.daymark.app.domain.models.Goal
import com.daymark.app.domain.models.GoalCheckIn
import com.daymark.app.domain.models.StreakInfo
import kotlin.math.max
import kotlin.math.min

object StreakCalculator {

    /**
     * Calculates streak information for a goal given its check-ins.
     * Respects:
     * - Independent goal schedule
     * - Preserves missed days without erasing history
     * - Completion rate: Completed Scheduled Days / Total Scheduled Days * 100
     * - Max 6 active goals philosophy
     */
    fun calculate(
        goal: Goal,
        checkIns: List<GoalCheckIn>,
        todayEpochDay: Long = DateTimeUtils.todayEpochDay()
    ): StreakInfo {
        if (checkIns.isEmpty()) {
            val totalScheduled = calculateTotalScheduledDays(goal.startDate, goal.durationDays, todayEpochDay)
            val remaining = calculateRemainingDays(goal.startDate, goal.durationDays, todayEpochDay)
            return StreakInfo(
                currentStreak = 0,
                bestStreak = 0,
                completedDays = 0,
                missedDays = totalScheduled,
                totalScheduledDays = totalScheduled,
                completionRate = 0f,
                remainingDays = remaining,
                todayCompleted = false,
                todayProgress = 0.0
            )
        }

        // Map completed check-ins by localDate
        val checkInMap = checkIns.associateBy { it.localDate }
        val completedDatesSet = checkIns.filter { it.completed }.map { it.localDate }.toSet()

        val todayCheckIn = checkInMap[todayEpochDay]
        val todayCompleted = todayCheckIn?.completed == true
        val todayProgress = todayCheckIn?.recordedValue ?: 0.0

        val completedDaysCount = completedDatesSet.size

        // Total scheduled days up to today or duration end
        val totalScheduledDays = calculateTotalScheduledDays(goal.startDate, goal.durationDays, todayEpochDay)
        val missedDaysCount = max(0, totalScheduledDays - completedDaysCount)

        val completionRate = if (totalScheduledDays > 0) {
            (completedDaysCount.toFloat() / totalScheduledDays.toFloat()) * 100f
        } else {
            0f
        }

        val remainingDays = calculateRemainingDays(goal.startDate, goal.durationDays, todayEpochDay)

        // Current Streak Calculation:
        // A streak is unbroken if either today is completed, or today is not yet over and yesterday was completed.
        var currentStreak = 0
        var checkDay = if (todayCompleted) todayEpochDay else todayEpochDay - 1

        while (checkDay >= goal.startDate && completedDatesSet.contains(checkDay)) {
            currentStreak++
            checkDay--
        }

        // Best Streak Calculation:
        // Find longest contiguous run of completed dates in sorted list
        var bestStreak = 0
        var tempStreak = 0
        val sortedCompletedDays = completedDatesSet.sorted()

        var previousDay: Long? = null
        for (day in sortedCompletedDays) {
            if (previousDay == null || day == previousDay + 1) {
                tempStreak++
            } else {
                tempStreak = 1
            }
            if (tempStreak > bestStreak) {
                bestStreak = tempStreak
            }
            previousDay = day
        }

        bestStreak = max(bestStreak, currentStreak)

        return StreakInfo(
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            completedDays = completedDaysCount,
            missedDays = missedDaysCount,
            totalScheduledDays = totalScheduledDays,
            completionRate = completionRate,
            remainingDays = remainingDays,
            todayCompleted = todayCompleted,
            todayProgress = todayProgress
        )
    }

    fun calculateTotalScheduledDays(startDate: Long, durationDays: Int, todayEpochDay: Long): Int {
        val effectiveEnd = min(todayEpochDay, startDate + durationDays - 1)
        return if (effectiveEnd >= startDate) {
            (effectiveEnd - startDate + 1).toInt()
        } else {
            0
        }
    }

    fun calculateRemainingDays(startDate: Long, durationDays: Int, todayEpochDay: Long): Int {
        val goalEndDay = startDate + durationDays - 1
        return max(0, (goalEndDay - todayEpochDay + 1).toInt())
    }
}
