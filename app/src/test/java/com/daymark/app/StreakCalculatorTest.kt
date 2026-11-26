package com.daymark.app

import com.daymark.app.domain.models.Goal
import com.daymark.app.domain.models.GoalCheckIn
import com.daymark.app.domain.models.TrackingType
import com.daymark.app.domain.usecases.StreakCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StreakCalculatorTest {

    @Test
    fun testEmptyCheckInsYieldsZeroStreak() {
        val goal = Goal(
            id = 1,
            title = "Meditate",
            startDate = 100L,
            durationDays = 30
        )

        val streak = StreakCalculator.calculate(goal, emptyList(), todayEpochDay = 105L)
        assertEquals(0, streak.currentStreak)
        assertEquals(0, streak.bestStreak)
        assertEquals(0, streak.completedDays)
        assertEquals(6, streak.totalScheduledDays) // Days 100, 101, 102, 103, 104, 105 = 6 days
        assertEquals(6, streak.missedDays)
        assertEquals(0f, streak.completionRate, 0.01f)
        assertFalse(streak.todayCompleted)
    }

    @Test
    fun testConsecutiveStreakEndingToday() {
        val goal = Goal(
            id = 1,
            title = "Read 10 pages",
            startDate = 100L,
            durationDays = 30
        )

        val today = 105L
        val checkIns = listOf(
            GoalCheckIn(goalId = 1, localDate = 103L, completed = true, recordedValue = 1.0),
            GoalCheckIn(goalId = 1, localDate = 104L, completed = true, recordedValue = 1.0),
            GoalCheckIn(goalId = 1, localDate = 105L, completed = true, recordedValue = 1.0)
        )

        val streak = StreakCalculator.calculate(goal, checkIns, todayEpochDay = today)
        assertEquals(3, streak.currentStreak)
        assertEquals(3, streak.bestStreak)
        assertEquals(3, streak.completedDays)
        assertTrue(streak.todayCompleted)
        assertEquals(6, streak.totalScheduledDays)
        // 3 of 6 = 50%
        assertEquals(50f, streak.completionRate, 0.01f)
    }

    @Test
    fun testStreakActiveWhenYesterdayCompletedButTodayPending() {
        val goal = Goal(
            id = 1,
            title = "Morning Walk",
            startDate = 100L,
            durationDays = 30
        )

        val today = 105L
        // Completed up to yesterday (104), today (105) not checked in yet
        val checkIns = listOf(
            GoalCheckIn(goalId = 1, localDate = 102L, completed = true),
            GoalCheckIn(goalId = 1, localDate = 103L, completed = true),
            GoalCheckIn(goalId = 1, localDate = 104L, completed = true)
        )

        val streak = StreakCalculator.calculate(goal, checkIns, todayEpochDay = today)
        // Streak is preserved from yesterday!
        assertEquals(3, streak.currentStreak)
        assertFalse(streak.todayCompleted)
    }

    @Test
    fun testMissedDayBreaksCurrentStreakButPreservesBestStreakAndHistory() {
        val goal = Goal(
            id = 1,
            title = "Workout",
            startDate = 100L,
            durationDays = 30
        )

        val today = 106L
        // 100, 101, 102, 103 completed (4 days streak)
        // 104 missed!
        // 105, 106 completed (2 days streak)
        val checkIns = listOf(
            GoalCheckIn(goalId = 1, localDate = 100L, completed = true),
            GoalCheckIn(goalId = 1, localDate = 101L, completed = true),
            GoalCheckIn(goalId = 1, localDate = 102L, completed = true),
            GoalCheckIn(goalId = 1, localDate = 103L, completed = true),
            GoalCheckIn(goalId = 1, localDate = 105L, completed = true),
            GoalCheckIn(goalId = 1, localDate = 106L, completed = true)
        )

        val streak = StreakCalculator.calculate(goal, checkIns, todayEpochDay = today)
        assertEquals(2, streak.currentStreak) // Current is 105 and 106
        assertEquals(4, streak.bestStreak)    // Best was 100..103
        assertEquals(6, streak.completedDays) // Total completed preserved!
        assertEquals(1, streak.missedDays)    // Exactly 1 missed day (104)
        assertEquals(7, streak.totalScheduledDays)
        assertEquals((6f / 7f) * 100f, streak.completionRate, 0.01f)
    }

    @Test
    fun testMeasurableGoalPartialProgress() {
        val goal = Goal(
            id = 2,
            title = "Drink 2000ml water",
            trackingType = TrackingType.MEASURABLE,
            targetValue = 2000.0,
            unit = "ml",
            startDate = 100L,
            durationDays = 14
        )

        val today = 100L
        val checkIns = listOf(
            GoalCheckIn(goalId = 2, localDate = 100L, recordedValue = 1500.0, completed = false)
        )

        val streak = StreakCalculator.calculate(goal, checkIns, todayEpochDay = today)
        assertFalse(streak.todayCompleted)
        assertEquals(1500.0, streak.todayProgress, 0.01)
        assertEquals(0, streak.completedDays)
    }
}
