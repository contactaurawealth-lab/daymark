package com.daymark.app.data.repositories

import com.daymark.app.core.database.dao.TaskDao
import com.daymark.app.core.database.dao.TaskListDao
import com.daymark.app.core.database.entities.TaskEntity
import com.daymark.app.domain.models.Priority
import com.daymark.app.domain.models.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepository(
    private val taskDao: TaskDao,
    private val taskListDao: TaskListDao
) {
    fun getAllTasks(): Flow<List<Task>> =
        taskDao.getAllTasks().map { entities -> entities.map { it.toDomain() } }

    fun getPendingTasks(): Flow<List<Task>> =
        taskDao.getPendingTasks().map { entities -> entities.map { it.toDomain() } }

    fun getCompletedTasks(): Flow<List<Task>> =
        taskDao.getCompletedTasks().map { entities -> entities.map { it.toDomain() } }

    suspend fun getTaskById(id: Long): Task? =
        taskDao.getTaskById(id)?.toDomain()

    fun getTaskByIdFlow(id: Long): Flow<Task?> =
        taskDao.getTaskByIdFlow(id).map { it?.toDomain() }

    suspend fun insertTask(task: Task): Long {
        val entity = TaskEntity.fromDomain(task)
        return taskDao.insertTask(entity)
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(TaskEntity.fromDomain(task))
    }

    suspend fun deleteTask(id: Long) {
        taskDao.deleteTaskById(id)
    }

    suspend fun toggleTaskCompletion(id: Long) {
        val task = taskDao.getTaskById(id) ?: return
        val newCompleted = !task.completed
        val completedAt = if (newCompleted) System.currentTimeMillis() else null
        taskDao.updateTaskCompletion(id, newCompleted, completedAt)
    }

    suspend fun duplicateTask(id: Long): Task? {
        val original = taskDao.getTaskById(id) ?: return null
        val duplicated = original.copy(
            id = 0,
            title = "${original.title} (Copy)",
            completed = false,
            completedAt = null,
            createdAt = System.currentTimeMillis()
        )
        val newId = taskDao.insertTask(duplicated)
        return duplicated.copy(id = newId).toDomain()
    }

    suspend fun reorderTasks(tasks: List<Task>) {
        tasks.forEachIndexed { index, task ->
            taskDao.updateSortOrder(task.id, index)
        }
    }
}
