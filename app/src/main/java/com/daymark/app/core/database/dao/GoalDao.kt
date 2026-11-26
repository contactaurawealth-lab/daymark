package com.daymark.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.daymark.app.core.database.entities.GoalEntity
import com.daymark.app.domain.models.GoalStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY id DESC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE status = 'ACTIVE' ORDER BY id ASC")
    fun getActiveGoals(): Flow<List<GoalEntity>>

    @Query("SELECT COUNT(*) FROM goals WHERE status = 'ACTIVE'")
    suspend fun getActiveGoalsCount(): Int

    @Query("SELECT COUNT(*) FROM goals WHERE status = 'ACTIVE'")
    fun getActiveGoalsCountFlow(): Flow<Int>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: Long): GoalEntity?

    @Query("SELECT * FROM goals WHERE id = :id")
    fun getGoalByIdFlow(id: Long): Flow<GoalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoalById(id: Long)

    @Query("UPDATE goals SET status = :status WHERE id = :id")
    suspend fun updateGoalStatus(id: Long, status: GoalStatus)

    @Query("DELETE FROM goals")
    suspend fun clearAll()
}
