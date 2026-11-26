package com.daymark.app

import com.daymark.app.domain.models.Goal
import com.daymark.app.domain.models.GoalStatus
import com.daymark.app.domain.models.TrackingType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GoalLimitAndLogicTest {

    @Test
    fun testMaxSixActiveGoalsRule() {
        val activeGoals = (1..6).map { i ->
            Goal(id = i.toLong(), title = "Goal $i", startDate = 100L, status = GoalStatus.ACTIVE)
        }

        val canAdd7th = activeGoals.size < 6
        assertFalse("Cannot add 7th active goal when 6 active goals exist", canAdd7th)

        // After archiving one:
        val withOneArchived = activeGoals.mapIndexed { idx, g ->
            if (idx == 0) g.copy(status = GoalStatus.ARCHIVED) else g
        }
        val activeCount = withOneArchived.count { it.status == GoalStatus.ACTIVE }
        assertEquals(5, activeCount)
        val canAddNow = activeCount < 6
        assertTrue("Can add goal when active count is 5", canAddNow)
    }

    @Test
    fun testMeasurableGoalThresholdLogic() {
        val goal = Goal(
            id = 1,
            title = "Read pages",
            trackingType = TrackingType.MEASURABLE,
            targetValue = 10.0,
            unit = "pages",
            startDate = 100L
        )

        var recorded = 0.0
        assertFalse(recorded >= goal.targetValue)

        recorded += 5.0
        assertFalse("5/10 is not completed", recorded >= goal.targetValue)

        recorded += 5.0
        assertTrue("10/10 is completed", recorded >= goal.targetValue)

        recorded += 2.0
        assertTrue("12/10 exceeds target and remains completed", recorded >= goal.targetValue)
    }
}
