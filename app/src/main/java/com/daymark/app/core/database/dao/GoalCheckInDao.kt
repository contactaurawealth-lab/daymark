package com.daymark.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.daymark.app.core.database.entities.GoalCheckInEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalCheckInDao {
    @Query("SELECT * FROM goal_check_ins WHERE goalId = :goalId ORDER BY localDate DESC")
    fun getCheckInsForGoal(goalId: Long): Flow<List<GoalCheckInEntity>>

    @Query("SELECT * FROM goal_check_ins WHERE goalId = :goalId ORDER BY localDate DESC")
    suspend fun getCheckInsListForGoal(goalId: Long): List<GoalCheckInEntity>

    @Query("SELECT * FROM goal_check_ins WHERE goalId = :goalId AND localDate = :localDate LIMIT 1")
    suspend fun getCheckInForGoalAndDate(goalId: Long, localDate: Long): GoalCheckInEntity?

    @Query("SELECT * FROM goal_check_ins WHERE goalId = :goalId AND localDate = :localDate LIMIT 1")
    fun getCheckInForGoalAndDateFlow(goalId: Long, localDate: Long): Flow<GoalCheckInEntity?>

    @Query("SELECT * FROM goal_check_ins WHERE localDate = :localDate")
    fun getAllCheckInsForDate(localDate: Long): Flow<List<GoalCheckInEntity>>

    @Query("SELECT * FROM goal_check_ins")
    fun getAllCheckIns(): Flow<List<GoalCheckInEntity>>

    @Query("SELECT * FROM goal_check_ins")
    suspend fun getAllCheckInsSync(): List<GoalCheckInEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCheckIn(checkIn: GoalCheckInEntity): Long

    @Query("DELETE FROM goal_check_ins WHERE goalId = :goalId AND localDate = :localDate")
    suspend fun deleteCheckIn(goalId: Long, localDate: Long)

    @Query("SELECT COUNT(*) FROM goal_check_ins WHERE goalId = :goalId AND completed = 1")
    suspend fun getCompletedCountForGoal(goalId: Long): Int

    @Query("DELETE FROM goal_check_ins")
    suspend fun clearAll()
}
