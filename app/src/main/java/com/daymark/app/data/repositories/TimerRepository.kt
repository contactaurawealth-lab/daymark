package com.daymark.app.data.repositories

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.daymark.app.core.database.dao.TaskDao
import com.daymark.app.core.database.dao.TimerDao
import com.daymark.app.core.database.entities.TimerSessionEntity
import com.daymark.app.domain.models.TimerSession
import com.daymark.app.domain.models.TimerStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.timerDataStore by preferencesDataStore(name = "daymark_timer")

data class PersistedTimerState(
    val taskId: Long? = null,
    val taskTitle: String = "",
    val targetEndTime: Long? = null, // System.currentTimeMillis() + remaining
    val remainingMillisOnPause: Long? = null,
    val totalDurationSeconds: Long = 1500, // 25 min default
    val status: TimerStatus = TimerStatus.IDLE
)

class TimerRepository(
    private val context: Context,
    private val timerDao: TimerDao,
    private val taskDao: TaskDao
) {
    private object Keys {
        val TASK_ID = longPreferencesKey("timer_task_id")
        val TASK_TITLE = stringPreferencesKey("timer_task_title")
        val TARGET_END_TIME = longPreferencesKey("timer_target_end_time")
        val REMAINING_PAUSE = longPreferencesKey("timer_remaining_pause")
        val TOTAL_DURATION = longPreferencesKey("timer_total_duration")
        val STATUS = stringPreferencesKey("timer_status")
    }

    val persistedState: Flow<PersistedTimerState> = context.timerDataStore.data.map { prefs ->
        val taskId = prefs[Keys.TASK_ID]
        val statusStr = prefs[Keys.STATUS] ?: TimerStatus.IDLE.name
        val status = try {
            TimerStatus.valueOf(statusStr)
        } catch (e: Exception) {
            TimerStatus.IDLE
        }

        PersistedTimerState(
            taskId = taskId,
            taskTitle = prefs[Keys.TASK_TITLE] ?: "",
            targetEndTime = prefs[Keys.TARGET_END_TIME],
            remainingMillisOnPause = prefs[Keys.REMAINING_PAUSE],
            totalDurationSeconds = prefs[Keys.TOTAL_DURATION] ?: 1500L,
            status = status
        )
    }

    suspend fun saveTimerState(state: PersistedTimerState) {
        context.timerDataStore.edit { prefs ->
            if (state.taskId != null) {
                prefs[Keys.TASK_ID] = state.taskId
            } else {
                prefs.remove(Keys.TASK_ID)
            }
            prefs[Keys.TASK_TITLE] = state.taskTitle
            if (state.targetEndTime != null) {
                prefs[Keys.TARGET_END_TIME] = state.targetEndTime
            } else {
                prefs.remove(Keys.TARGET_END_TIME)
            }
            if (state.remainingMillisOnPause != null) {
                prefs[Keys.REMAINING_PAUSE] = state.remainingMillisOnPause
            } else {
                prefs.remove(Keys.REMAINING_PAUSE)
            }
            prefs[Keys.TOTAL_DURATION] = state.totalDurationSeconds
            prefs[Keys.STATUS] = state.status.name
        }
    }

    suspend fun clearTimerState() {
        context.timerDataStore.edit { it.clear() }
    }

    suspend fun recordSession(
        taskId: Long,
        durationSeconds: Long,
        elapsedSeconds: Long,
        status: TimerStatus
    ): Long {
        val completedAt = if (status == TimerStatus.COMPLETED) System.currentTimeMillis() else null
        val entity = TimerSessionEntity(
            taskId = taskId,
            durationSeconds = durationSeconds,
            startedAt = System.currentTimeMillis() - (elapsedSeconds * 1000),
            elapsedSeconds = elapsedSeconds,
            status = status,
            completedAt = completedAt
        )
        return timerDao.insertSession(entity)
    }

    fun getSessionsForTask(taskId: Long): Flow<List<TimerSession>> =
        timerDao.getSessionsForTask(taskId).map { entities ->
            val task = taskDao.getTaskById(taskId)
            entities.map { it.toDomain(task?.title ?: "") }
        }

    suspend fun getTotalElapsedForTask(taskId: Long): Long =
        timerDao.getTotalElapsedForTask(taskId) ?: 0L

    fun getTotalElapsedForTaskFlow(taskId: Long): Flow<Long> =
        timerDao.getTotalElapsedForTaskFlow(taskId).map { it ?: 0L }
}
