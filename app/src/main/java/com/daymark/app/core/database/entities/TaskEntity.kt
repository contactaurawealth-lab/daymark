package com.daymark.app.core.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.daymark.app.domain.models.Priority
import com.daymark.app.domain.models.Task

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["completed"]),
        Index(value = ["dueDate"]),
        Index(value = ["listId"]),
        Index(value = ["sortOrder"])
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val dueDate: Long? = null,
    val priority: Priority = Priority.MEDIUM,
    val completed: Boolean = false,
    val completedAt: Long? = null,
    val listId: Long? = null,
    val sortOrder: Int = 0,
    val reminderTime: Long? = null
) {
    fun toDomain(): Task = Task(
        id = id,
        title = title,
        description = description,
        createdAt = createdAt,
        dueDate = dueDate,
        priority = priority,
        completed = completed,
        completedAt = completedAt,
        listId = listId,
        sortOrder = sortOrder,
        reminderTime = reminderTime
    )

    companion object {
        fun fromDomain(task: Task): TaskEntity = TaskEntity(
            id = task.id,
            title = task.title,
            description = task.description,
            createdAt = task.createdAt,
            dueDate = task.dueDate,
            priority = task.priority,
            completed = task.completed,
            completedAt = task.completedAt,
            listId = task.listId,
            sortOrder = task.sortOrder,
            reminderTime = task.reminderTime
        )
    }
}
