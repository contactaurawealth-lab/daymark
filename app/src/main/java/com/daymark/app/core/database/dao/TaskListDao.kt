package com.daymark.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.daymark.app.core.database.entities.TaskListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskListDao {
    @Query("SELECT * FROM task_lists ORDER BY sortOrder ASC, id ASC")
    fun getAllLists(): Flow<List<TaskListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: TaskListEntity): Long

    @Update
    suspend fun updateList(list: TaskListEntity)

    @Delete
    suspend fun deleteList(list: TaskListEntity)

    @Query("DELETE FROM task_lists")
    suspend fun clearAll()
}
