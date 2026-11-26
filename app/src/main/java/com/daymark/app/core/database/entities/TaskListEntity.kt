package com.daymark.app.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.daymark.app.domain.models.TaskList

@Entity(tableName = "task_lists")
data class TaskListEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sortOrder: Int = 0
) {
    fun toDomain(): TaskList = TaskList(id = id, name = name, sortOrder = sortOrder)

    companion object {
        fun fromDomain(list: TaskList): TaskListEntity = TaskListEntity(
            id = list.id,
            name = list.name,
            sortOrder = list.sortOrder
        )
    }
}
