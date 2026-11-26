package com.daymark.app.data.repositories

import com.daymark.app.core.database.dao.GoalCheckInDao
import com.daymark.app.core.database.dao.GoalDao
import com.daymark.app.core.database.entities.GoalCheckInEntity
import com.daymark.app.core.database.entities.GoalEntity
import com.daymark.app.core.utilities.DateTimeUtils
import com.daymark.app.domain.models.Goal
import com.daymark.app.domain.models.GoalCheckIn
import com.daymark.app.domain.models.GoalStatus
import com.daymark.app.domain.models.StreakInfo
import com.daymark.app.domain.models.TrackingType
import com.daymark.app.domain.usecases.StreakCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GoalRepository(
    private val goalDao: GoalDao,
    private val goalCheckInDao: GoalCheckInDao
) {
    companion object {
        const val MAX_ACTIVE_GOALS = 6
    }

    fun getAllGoals(): Flow<List<Goal>> =
        goalDao.getAllGoals().map { entities -> entities.map { it.toDomain() } }

    fun getActiveGoals(): Flow<List<Goal>> =
        goalDao.getActiveGoals().map { entities -> entities.map { it.toDomain() } }

    fun getActiveGoalsCountFlow(): Flow<Int> = goalDao.getActiveGoalsCountFlow()

    suspend fun getActiveGoalsCount(): Int = goalDao.getActiveGoalsCount()

    suspend fun canAddActiveGoal(): Boolean = getActiveGoalsCount() < MAX_ACTIVE_GOALS

    suspend fun insertGoal(goal: Goal): Result<Long> {
        if (goal.status == GoalStatus.ACTIVE && !canAddActiveGoal()) {
            return Result.failure(
                IllegalStateException("Maximum of $MAX_ACTIVE_GOALS active goals reached. Archive an existing goal to create a new one.")
            )
        }
        val id = goalDao.insertGoal(GoalEntity.fromDomain(goal))
        return Result.success(id)
    }

    suspend fun updateGoal(goal: Goal): Result<Unit> {
        val existing = goalDao.getGoalById(goal.id)
        if (existing != null && existing.status != GoalStatus.ACTIVE && goal.status == GoalStatus.ACTIVE) {
            if (!canAddActiveGoal()) {
                return Result.failure(
                    IllegalStateException("Cannot activate goal: maximum of $MAX_ACTIVE_GOALS active goals reached.")
                )
            }
        }
        goalDao.updateGoal(GoalEntity.fromDomain(goal))
        return Result.success(Unit)
    }

    suspend fun deleteGoal(id: Long) {
        goalDao.deleteGoalById(id)
    }

    suspend fun archiveGoal(id: Long) {
        goalDao.updateGoalStatus(id, GoalStatus.ARCHIVED)
    }

    suspend fun pauseGoal(id: Long) {
        goalDao.updateGoalStatus(id, GoalStatus.PAUSED)
    }

    suspend fun resumeGoal(id: Long): Result<Unit> {
        if (!canAddActiveGoal()) {
            return Result.failure(
                IllegalStateException("Cannot resume goal: maximum of $MAX_ACTIVE_GOALS active goals reached.")
            )
        }
        goalDao.updateGoalStatus(id, GoalStatus.ACTIVE)
        return Result.success(Unit)
    }

    suspend fun getGoalById(id: Long): Goal? =
        goalDao.getGoalById(id)?.toDomain()

    fun getGoalByIdFlow(id: Long): Flow<Goal?> =
        goalDao.getGoalByIdFlow(id).map { it?.toDomain() }

    fun getCheckInsForGoal(goalId: Long): Flow<List<GoalCheckIn>> =
        goalCheckInDao.getCheckInsForGoal(goalId).map { entities -> entities.map { it.toDomain() } }

    fun getTodayCheckInFlow(goalId: Long): Flow<GoalCheckIn?> =
        goalCheckInDao.getCheckInForGoalAndDateFlow(goalId, DateTimeUtils.todayEpochDay())
            .map { it?.toDomain() }

    fun getTodayCheckInsFlow(): Flow<List<GoalCheckIn>> =
        goalCheckInDao.getAllCheckInsForDate(DateTimeUtils.todayEpochDay())
            .map { list -> list.map { it.toDomain() } }

    suspend fun getTodayCheckIn(goalId: Long): GoalCheckIn? =
        goalCheckInDao.getCheckInForGoalAndDate(goalId, DateTimeUtils.todayEpochDay())?.toDomain()

    suspend fun checkInGoal(
        goalId: Long,
        date: Long = DateTimeUtils.todayEpochDay(),
        recordedValue: Double,
        completed: Boolean
    ): Long {
        val entity = GoalCheckInEntity(
            goalId = goalId,
            localDate = date,
            recordedValue = recordedValue,
            completed = completed,
            createdAt = System.currentTimeMillis()
        )
        return goalCheckInDao.insertOrUpdateCheckIn(entity)
    }

    suspend fun toggleCheckboxGoal(goalId: Long, date: Long = DateTimeUtils.todayEpochDay()): Boolean {
        val existing = goalCheckInDao.getCheckInForGoalAndDate(goalId, date)
        val newCompleted = !(existing?.completed ?: false)
        val newValue = if (newCompleted) 1.0 else 0.0
        val entity = GoalCheckInEntity(
            id = existing?.id ?: 0,
            goalId = goalId,
            localDate = date,
            recordedValue = newValue,
            completed = newCompleted,
            createdAt = existing?.createdAt ?: System.currentTimeMillis()
        )
        goalCheckInDao.insertOrUpdateCheckIn(entity)
        return newCompleted
    }

    suspend fun recordMeasurableProgress(
        goal: Goal,
        addedValue: Double,
        date: Long = DateTimeUtils.todayEpochDay()
    ): GoalCheckIn {
        val existing = goalCheckInDao.getCheckInForGoalAndDate(goal.id, date)
        val newTotal = (existing?.recordedValue ?: 0.0) + addedValue
        val isCompleted = newTotal >= goal.targetValue
        val entity = GoalCheckInEntity(
            id = existing?.id ?: 0,
            goalId = goal.id,
            localDate = date,
            recordedValue = newTotal,
            completed = isCompleted,
            createdAt = existing?.createdAt ?: System.currentTimeMillis()
        )
        val id = goalCheckInDao.insertOrUpdateCheckIn(entity)
        return entity.copy(id = id).toDomain()
    }

    suspend fun calculateStreakInfo(goal: Goal): StreakInfo {
        val checkIns = goalCheckInDao.getCheckInsListForGoal(goal.id).map { it.toDomain() }
        return StreakCalculator.calculate(goal, checkIns)
    }
}
