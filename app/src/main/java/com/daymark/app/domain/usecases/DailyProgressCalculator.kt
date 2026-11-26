package com.daymark.app.domain.usecases

import com.daymark.app.core.utilities.DateTimeUtils
import com.daymark.app.domain.models.DailyProgress
import com.daymark.app.domain.models.Goal
import com.daymark.app.domain.models.GoalCheckIn
import com.daymark.app.domain.models.Task

object DailyProgressCalculator {

    fun calculate(
        tasks: List<Task>,
        goals: List<Goal>,
        todayCheckIns: List<GoalCheckIn>,
        todayEpochDay: Long = DateTimeUtils.todayEpochDay()
    ): DailyProgress {
        val todayStartMillis = todayEpochDay * 86400000L
        val todayEndMillis = todayStartMillis + 86400000L

        // Tasks completed today: either completedAt falls on today, or marked complete and due today
        val completedToday = tasks.filter { task ->
            task.completed && (
                (task.completedAt != null && task.completedAt in todayStartMillis until todayEndMillis) ||
                (task.dueDate != null && task.dueDate / 86400000L == todayEpochDay) ||
                (task.completedAt == null && task.dueDate == null) // Completed task without timestamps
            )
        }.size

        // Tasks relevant for today: pending tasks due on or before today, plus completed today
        val pendingDueToday = tasks.filter { task ->
            !task.completed && (task.dueDate == null || task.dueDate / 86400000L <= todayEpochDay)
        }.size

        val totalTasksToday = completedToday + pendingDueToday

        val taskRate = if (totalTasksToday > 0) {
            (completedToday.toFloat() / totalTasksToday.toFloat()) * 100f
        } else {
            100f // 100% when no pending tasks remain
        }

        // Active Goals
        val activeGoals = goals.filter { it.status == com.daymark.app.domain.models.GoalStatus.ACTIVE }
        val activeGoalIds = activeGoals.map { it.id }.toSet()

        val completedGoalsToday = todayCheckIns.filter { checkIn ->
            checkIn.completed && activeGoalIds.contains(checkIn.goalId)
        }.size

        val goalRate = if (activeGoals.isNotEmpty()) {
            (completedGoalsToday.toFloat() / activeGoals.size.toFloat()) * 100f
        } else {
            100f
        }

        // Weighted Overall: Tasks 50%, Goals 50%
        val overallRate = if (totalTasksToday > 0 && activeGoals.isNotEmpty()) {
            (taskRate * 0.5f) + (goalRate * 0.5f)
        } else if (totalTasksToday > 0) {
            taskRate
        } else if (activeGoals.isNotEmpty()) {
            goalRate
        } else {
            100f
        }

        return DailyProgress(
            completedTasksToday = completedToday,
            totalTasksToday = totalTasksToday,
            taskCompletionRate = taskRate,
            completedGoalsToday = completedGoalsToday,
            totalActiveGoals = activeGoals.size,
            goalCompletionRate = goalRate,
            overallCompletionRate = overallRate
        )
    }
}
