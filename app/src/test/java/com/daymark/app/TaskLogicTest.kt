package com.daymark.app

import com.daymark.app.domain.models.Priority
import com.daymark.app.domain.models.Task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskLogicTest {

    @Test
    fun testTaskCreationAndDefaults() {
        val task = Task(
            title = "Test Task"
        )
        assertEquals("Test Task", task.title)
        assertNull(task.description)
        assertEquals(Priority.MEDIUM, task.priority)
        assertFalse(task.completed)
        assertNull(task.completedAt)
    }

    @Test
    fun testTaskCompletionToggle() {
        val task = Task(title = "Read a book", completed = false)
        val completedAt = System.currentTimeMillis()
        val completedTask = task.copy(completed = true, completedAt = completedAt)

        assertTrue(completedTask.completed)
        assertEquals(completedAt, completedTask.completedAt)

        val reopenedTask = completedTask.copy(completed = false, completedAt = null)
        assertFalse(reopenedTask.completed)
        assertNull(reopenedTask.completedAt)
    }

    @Test
    fun testTaskDuplication() {
        val task = Task(
            id = 42,
            title = "Daily Review",
            description = "Check email and tickets",
            priority = Priority.HIGH,
            completed = true
        )

        val duplicate = task.copy(
            id = 0,
            title = "${task.title} (Copy)",
            completed = false,
            completedAt = null
        )

        assertEquals(0L, duplicate.id)
        assertEquals("Daily Review (Copy)", duplicate.title)
        assertEquals("Check email and tickets", duplicate.description)
        assertEquals(Priority.HIGH, duplicate.priority)
        assertFalse(duplicate.completed)
    }

    @Test
    fun testTaskOrdering() {
        val tasks = listOf(
            Task(id = 1, title = "Task 1", sortOrder = 2),
            Task(id = 2, title = "Task 2", sortOrder = 0),
            Task(id = 3, title = "Task 3", sortOrder = 1)
        )

        val sorted = tasks.sortedBy { it.sortOrder }
        assertEquals(2L, sorted[0].id)
        assertEquals(3L, sorted[1].id)
        assertEquals(1L, sorted[2].id)
    }
}
