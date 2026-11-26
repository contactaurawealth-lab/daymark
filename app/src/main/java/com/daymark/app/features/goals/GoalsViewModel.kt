package com.daymark.app.features.goals

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.daymark.app.core.utilities.DateTimeUtils
import com.daymark.app.data.repositories.GoalRepository
import com.daymark.app.domain.models.Goal
import com.daymark.app.domain.models.GoalStatus
import com.daymark.app.domain.models.StreakInfo
import com.daymark.app.domain.models.TrackingType
import com.daymark.app.domain.usecases.StreakCalculator
import com.daymark.app.features.widgets.WidgetUpdateHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GoalItemState(
    val goal: Goal,
    val streakInfo: StreakInfo,
    val todayCompleted: Boolean,
    val todayProgress: Double
)

private data class GoalsDialogState(
    val selectedGoalId: Long? = null,
    val showCreateDialog: Boolean = false,
    val showLimitReachedDialog: Boolean = false,
    val errorMessage: String? = null
)

data class GoalsUiState(
    val activeGoals: List<GoalItemState> = emptyList(),
    val archivedGoals: List<GoalItemState> = emptyList(),
    val activeGoalsCount: Int = 0,
    val canAddGoal: Boolean = true,
    val selectedGoalDetail: GoalItemState? = null,
    val showCreateDialog: Boolean = false,
    val showLimitReachedDialog: Boolean = false,
    val errorMessage: String? = null
)

class GoalsViewModel(
    private val goalRepository: GoalRepository,
    private val appContext: Context
) : ViewModel() {

    private val _dialogState = MutableStateFlow(GoalsDialogState())

    val uiState: StateFlow<GoalsUiState> = combine(
        goalRepository.getAllGoals(),
        goalRepository.getAllCheckInsFlow(),
        _dialogState
    ) { allGoals, allCheckIns, dialog ->
        val checkInsByGoal = allCheckIns.groupBy { it.goalId }
        val todayEpochDay = DateTimeUtils.todayEpochDay()

        val activeList = mutableListOf<GoalItemState>()
        val archivedList = mutableListOf<GoalItemState>()

        for (goal in allGoals) {
            val goalCheckIns = checkInsByGoal[goal.id].orEmpty()
            val streak = StreakCalculator.calculate(goal, goalCheckIns, todayEpochDay)
            val item = GoalItemState(
                goal = goal,
                streakInfo = streak,
                todayCompleted = streak.todayCompleted,
                todayProgress = streak.todayProgress
            )
            if (goal.status == GoalStatus.ACTIVE) {
                activeList.add(item)
            } else {
                archivedList.add(item)
            }
        }

        val activeCount = activeList.size
        val canAdd = activeCount < GoalRepository.MAX_ACTIVE_GOALS

        val updatedDetail = dialog.selectedGoalId?.let { selectedId ->
            (activeList + archivedList).find { it.goal.id == selectedId }
        }

        GoalsUiState(
            activeGoals = activeList,
            archivedGoals = archivedList,
            activeGoalsCount = activeCount,
            canAddGoal = canAdd,
            selectedGoalDetail = updatedDetail,
            showCreateDialog = dialog.showCreateDialog,
            showLimitReachedDialog = dialog.showLimitReachedDialog,
            errorMessage = dialog.errorMessage
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GoalsUiState())

    fun onRequestAddGoal() {
        viewModelScope.launch {
            if (goalRepository.canAddActiveGoal()) {
                _dialogState.update { it.copy(showCreateDialog = true) }
            } else {
                _dialogState.update { it.copy(showLimitReachedDialog = true) }
            }
        }
    }

    fun dismissCreateDialog() {
        _dialogState.update { it.copy(showCreateDialog = false) }
    }

    fun dismissLimitDialog() {
        _dialogState.update { it.copy(showLimitReachedDialog = false) }
    }

    fun clearError() {
        _dialogState.update { it.copy(errorMessage = null) }
    }

    fun createGoal(
        title: String,
        description: String? = null,
        icon: String = "sparkles",
        color: Long = 0xFF91A68A,
        trackingType: TrackingType = TrackingType.CHECKBOX,
        targetValue: Double = 1.0,
        unit: String = "times",
        durationDays: Int = 30
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val goal = Goal(
                title = title.trim(),
                description = description?.trim()?.ifBlank { null },
                icon = icon,
                color = color,
                trackingType = trackingType,
                targetValue = if (trackingType == TrackingType.CHECKBOX) 1.0 else targetValue,
                unit = unit.trim(),
                startDate = DateTimeUtils.todayEpochDay(),
                durationDays = durationDays,
                status = GoalStatus.ACTIVE
            )
            val result = goalRepository.insertGoal(goal)
            if (result.isSuccess) {
                _dialogState.update { it.copy(showCreateDialog = false) }
                WidgetUpdateHelper.updateAllWidgetsAsync(appContext)
            } else {
                _dialogState.update { it.copy(errorMessage = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun toggleCheckboxGoal(goalId: Long) {
        viewModelScope.launch {
            goalRepository.toggleCheckboxGoal(goalId)
            WidgetUpdateHelper.updateAllWidgetsAsync(appContext)
        }
    }

    fun recordMeasurableProgress(goal: Goal, amount: Double) {
        viewModelScope.launch {
            goalRepository.recordMeasurableProgress(goal, amount)
            WidgetUpdateHelper.updateAllWidgetsAsync(appContext)
        }
    }

    fun setMeasurableDirectValue(goal: Goal, directValue: Double) {
        viewModelScope.launch {
            val isCompleted = directValue >= goal.targetValue
            goalRepository.checkInGoal(
                goalId = goal.id,
                recordedValue = directValue,
                completed = isCompleted
            )
            WidgetUpdateHelper.updateAllWidgetsAsync(appContext)
        }
    }

    fun openGoalDetail(item: GoalItemState) {
        _dialogState.update { it.copy(selectedGoalId = item.goal.id) }
    }

    fun closeGoalDetail() {
        _dialogState.update { it.copy(selectedGoalId = null) }
    }

    fun archiveGoal(goalId: Long) {
        viewModelScope.launch {
            goalRepository.archiveGoal(goalId)
            closeGoalDetail()
            WidgetUpdateHelper.updateAllWidgetsAsync(appContext)
        }
    }

    fun resumeGoal(goalId: Long) {
        viewModelScope.launch {
            val res = goalRepository.resumeGoal(goalId)
            if (res.isFailure) {
                _dialogState.update { it.copy(errorMessage = res.exceptionOrNull()?.message) }
            } else {
                WidgetUpdateHelper.updateAllWidgetsAsync(appContext)
            }
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            goalRepository.deleteGoal(goalId)
            closeGoalDetail()
            WidgetUpdateHelper.updateAllWidgetsAsync(appContext)
        }
    }

    class Factory(
        private val goalRepository: GoalRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GoalsViewModel(goalRepository, context) as T
        }
    }
}
