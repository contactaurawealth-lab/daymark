package com.daymark.app

import com.daymark.app.domain.models.Goal
import com.daymark.app.domain.models.GoalCheckIn
import com.daymark.app.domain.models.GoalStatus
import com.daymark.app.domain.models.Task
import com.daymark.app.domain.usecases.DailyProgressCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyProgressCalculatorTest {

    private val todayEpoch = 19500L
    private val todayStartMillis = todayEpoch * 86400000L

    @Test
    fun testEmptyStateReturns100Percent() {
        val progress = DailyProgressCalculator.calculate(
            tasks = emptyList(),
            goals = emptyList(),
            todayCheckIns = emptyList(),
            todayEpochDay = todayEpoch
        )

        assertEquals(0, progress.completedTasksToday)
        assertEquals(0, progress.totalTasksToday)
        assertEquals(100f, progress.taskCompletionRate, 0.01f)
        assertEquals(100f, progress.overallCompletionRate, 0.01f)
    }

    @Test
    fun testTaskCompletionRate() {
        val tasks = listOf(
            Task(id = 1, title = "Task 1", completed = true, completedAt = todayStartMillis + 1000L),
            Task(id = 2, title = "Task 2", completed = false, dueDate = todayStartMillis),
            Task(id = 3, title = "Task 3", completed = false, dueDate = null), // No due date counts for today
            Task(id = 4, title = "Future Task", completed = false, dueDate = todayStartMillis + 86400000L * 2) // Future task not due today
        )

        val progress = DailyProgressCalculator.calculate(
            tasks = tasks,
            goals = emptyList(),
            todayCheckIns = emptyList(),
            todayEpochDay = todayEpoch
        )

        assertEquals(1, progress.completedTasksToday)
        assertEquals(3, progress.totalTasksToday) // 1 completed + 2 pending due today/no-due
        assertEquals(33.33f, progress.taskCompletionRate, 0.5f)
    }

    @Test
    fun testGoalCheckInRate() {
        val goals = listOf(
            Goal(id = 10, title = "Exercise", startDate = todayEpoch, status = GoalStatus.ACTIVE),
            Goal(id = 20, title = "Read", startDate = todayEpoch, status = GoalStatus.ACTIVE),
            Goal(id = 30, title = "Archived Goal", startDate = todayEpoch, status = GoalStatus.ARCHIVED)
        )

        val checkIns = listOf(
            GoalCheckIn(id = 1, goalId = 10, localDate = todayEpoch, completed = true, recordedValue = 1.0),
            GoalCheckIn(id = 2, goalId = 20, localDate = todayEpoch, completed = false, recordedValue = 0.0)
        )

        val progress = DailyProgressCalculator.calculate(
            tasks = emptyList(),
            goals = goals,
            todayCheckIns = checkIns,
            todayEpochDay = todayEpoch
        )

        assertEquals(1, progress.completedGoalsToday)
        assertEquals(2, progress.totalActiveGoals)
        assertEquals(50f, progress.goalCompletionRate, 0.01f)
        assertEquals(50f, progress.overallCompletionRate, 0.01f)
    }

    @Test
    fun testCombinedOverallRate() {
        // 2/2 tasks = 100%, 0/2 goals = 0% -> overall = 50%
        val tasks = listOf(
            Task(id = 1, title = "Task 1", completed = true, completedAt = todayStartMillis),
            Task(id = 2, title = "Task 2", completed = true, completedAt = todayStartMillis)
        )
        val goals = listOf(
            Goal(id = 10, title = "G1", startDate = todayEpoch, status = GoalStatus.ACTIVE),
            Goal(id = 20, title = "G2", startDate = todayEpoch, status = GoalStatus.ACTIVE)
        )

        val progress = DailyProgressCalculator.calculate(
            tasks = tasks,
            goals = goals,
            todayCheckIns = emptyList(),
            todayEpochDay = todayEpoch
        )

        assertEquals(100f, progress.taskCompletionRate, 0.01f)
        assertEquals(0f, progress.goalCompletionRate, 0.01f)
        assertEquals(50f, progress.overallCompletionRate, 0.01f)
    }
}
