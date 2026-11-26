package com.daymark.app.features.goals

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.daymark.app.core.utilities.DateTimeUtils
import com.daymark.app.data.repositories.GoalRepository
import com.daymark.app.domain.models.Goal
import com.daymark.app.domain.models.GoalCheckIn
import com.daymark.app.domain.models.GoalStatus
import com.daymark.app.domain.models.StreakInfo
import com.daymark.app.domain.models.TrackingType
import com.daymark.app.features.widgets.WidgetUpdateHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GoalItemState(
    val goal: Goal,
    val streakInfo: StreakInfo,
    val todayCompleted: Boolean,
    val todayProgress: Double
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

    private val _selectedGoalDetail = MutableStateFlow<GoalItemState?>(null)
    private val _showCreateDialog = MutableStateFlow(false)
    private val _showLimitReachedDialog = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<GoalsUiState> = combine(
        goalRepository.getAllGoals(),
        _selectedGoalDetail,
        _showCreateDialog,
        _showLimitReachedDialog,
        _errorMessage
    ) { allGoals, selectedDetail, showCreate, showLimit, error ->
        val activeList = mutableListOf<GoalItemState>()
        val archivedList = mutableListOf<GoalItemState>()

        for (goal in allGoals) {
            val streak = goalRepository.calculateStreakInfo(goal)
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

        // Refresh selected detail if open
        val updatedDetail = selectedDetail?.let { current ->
            (activeList + archivedList).find { it.goal.id == current.goal.id }
        }

        GoalsUiState(
            activeGoals = activeList,
            archivedGoals = archivedList,
            activeGoalsCount = activeCount,
            canAddGoal = canAdd,
            selectedGoalDetail = updatedDetail,
            showCreateDialog = showCreate,
            showLimitReachedDialog = showLimit,
            errorMessage = error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GoalsUiState())

    fun onRequestAddGoal() {
        viewModelScope.launch {
            if (goalRepository.canAddActiveGoal()) {
                _showCreateDialog.value = true
            } else {
                _showLimitReachedDialog.value = true
            }
        }
    }

    fun dismissCreateDialog() {
        _showCreateDialog.value = false
    }

    fun dismissLimitDialog() {
        _showLimitReachedDialog.value = false
    }

    fun clearError() {
        _errorMessage.value = null
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
                _showCreateDialog.value = false
                WidgetUpdateHelper.updateAllWidgets(appContext)
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun toggleCheckboxGoal(goalId: Long) {
        viewModelScope.launch {
            goalRepository.toggleCheckboxGoal(goalId)
            WidgetUpdateHelper.updateAllWidgets(appContext)
        }
    }

    fun recordMeasurableProgress(goal: Goal, amount: Double) {
        viewModelScope.launch {
            goalRepository.recordMeasurableProgress(goal, amount)
            WidgetUpdateHelper.updateAllWidgets(appContext)
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
            WidgetUpdateHelper.updateAllWidgets(appContext)
        }
    }

    fun openGoalDetail(item: GoalItemState) {
        _selectedGoalDetail.value = item
    }

    fun closeGoalDetail() {
        _selectedGoalDetail.value = null
    }

    fun archiveGoal(goalId: Long) {
        viewModelScope.launch {
            goalRepository.archiveGoal(goalId)
            closeGoalDetail()
            WidgetUpdateHelper.updateAllWidgets(appContext)
        }
    }

    fun resumeGoal(goalId: Long) {
        viewModelScope.launch {
            val res = goalRepository.resumeGoal(goalId)
            if (res.isFailure) {
                _errorMessage.value = res.exceptionOrNull()?.message
            } else {
                WidgetUpdateHelper.updateAllWidgets(appContext)
            }
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            goalRepository.deleteGoal(goalId)
            closeGoalDetail()
            WidgetUpdateHelper.updateAllWidgets(appContext)
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
