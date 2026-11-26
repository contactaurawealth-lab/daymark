package com.daymark.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.daymark.app.core.database.entities.TimerSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerDao {
    @Query("SELECT * FROM timer_sessions WHERE taskId = :taskId ORDER BY startedAt DESC")
    fun getSessionsForTask(taskId: Long): Flow<List<TimerSessionEntity>>

    @Query("SELECT * FROM timer_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<TimerSessionEntity>>

    @Query("SELECT * FROM timer_sessions")
    suspend fun getAllSessionsSync(): List<TimerSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TimerSessionEntity): Long

    @Update
    suspend fun updateSession(session: TimerSessionEntity)

    @Query("SELECT SUM(elapsedSeconds) FROM timer_sessions WHERE taskId = :taskId")
    suspend fun getTotalElapsedForTask(taskId: Long): Long?

    @Query("SELECT SUM(elapsedSeconds) FROM timer_sessions WHERE taskId = :taskId")
    fun getTotalElapsedForTaskFlow(taskId: Long): Flow<Long?>

    @Query("DELETE FROM timer_sessions")
    suspend fun clearAll()
}
