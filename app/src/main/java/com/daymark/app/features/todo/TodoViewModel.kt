package com.daymark.app.features.todo

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.daymark.app.core.utilities.DailyMotivationProvider
import com.daymark.app.core.utilities.DateTimeUtils
import com.daymark.app.data.repositories.GoalRepository
import com.daymark.app.data.repositories.SettingsRepository
import com.daymark.app.data.repositories.TaskRepository
import com.daymark.app.domain.models.AppSettings
import com.daymark.app.domain.models.DailyProgress
import com.daymark.app.domain.models.Goal
import com.daymark.app.domain.models.GoalCheckIn
import com.daymark.app.domain.models.MotivationalMessage
import com.daymark.app.domain.models.Priority
import com.daymark.app.domain.models.Task
import com.daymark.app.domain.usecases.DailyProgressCalculator
import com.daymark.app.features.notifications.DaymarkAlarmReceiver
import com.daymark.app.features.widgets.WidgetUpdateHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TodoDialogState(
    val showCompleted: Boolean = false,
    val activeTaskForTimer: Task? = null,
    val editingTask: Task? = null,
    val settingReminderForTask: Task? = null,
    val showMotivationSheet: Boolean = false,
    val showStandaloneTimerPicker: Boolean = false
)

private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

data class TodoUiState(
    val pendingTasks: List<Task> = emptyList(),
    val completedTasks: List<Task> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val currentDateFormatted: String = DateTimeUtils.formatTodayHeader(),
    val showCompletedSection: Boolean = false,
    val activeTaskForTimer: Task? = null,
    val editingTask: Task? = null,
    val settingReminderForTask: Task? = null,
    val showMotivationSheet: Boolean = false,
    val showStandaloneTimerPicker: Boolean = false,
    val dailyProgress: DailyProgress = DailyProgress(),
    val weeklyStats: com.daymark.app.domain.usecases.WeeklyProductivityStats = com.daymark.app.domain.usecases.WeeklyProductivityStats(),
    val dailyMotivation: MotivationalMessage = DailyMotivationProvider.getTodayMessage(),
    val appSettings: AppSettings = AppSettings()
)

class TodoViewModel(
    private val taskRepository: TaskRepository,
    private val goalRepository: GoalRepository,
    private val settingsRepository: SettingsRepository,
    private val appContext: Context
) : ViewModel() {

    private val _dialogState = MutableStateFlow(TodoDialogState())

    private val tasksFlow = combine(
        taskRepository.getAllTasks(),
        taskRepository.getPendingTasks(),
        taskRepository.getCompletedTasks()
    ) { all, pending, completed -> Triple(all, pending, completed) }

    private val otherFlow = combine(
        goalRepository.getActiveGoals(),
        goalRepository.getTodayCheckInsFlow(),
        settingsRepository.settingsFlow,
        _dialogState
    ) { goals, checkIns, settings, dialog ->
        Quad(goals, checkIns, settings, dialog)
    }

    val uiState: StateFlow<TodoUiState> = combine(
        tasksFlow,
        otherFlow
    ) { (allTasks, pending, completed), (goals, checkIns, settings, dialog) ->
        val progress = DailyProgressCalculator.calculate(allTasks, goals, checkIns)
        val weeklyStats = com.daymark.app.domain.usecases.WeeklyInsightsCalculator.calculate(allTasks)
        TodoUiState(
            pendingTasks = pending,
            completedTasks = completed,
            completedCount = completed.size,
            totalCount = pending.size + completed.size,
            currentDateFormatted = DateTimeUtils.formatTodayHeader(),
            showCompletedSection = dialog.showCompleted,
            activeTaskForTimer = dialog.activeTaskForTimer,
            editingTask = dialog.editingTask,
            settingReminderForTask = dialog.settingReminderForTask,
            showMotivationSheet = dialog.showMotivationSheet,
            showStandaloneTimerPicker = dialog.showStandaloneTimerPicker,
            dailyProgress = progress,
            weeklyStats = weeklyStats,
            dailyMotivation = DailyMotivationProvider.getTodayMessage(),
            appSettings = settings
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodoUiState())

    fun toggleTaskCompletion(taskId: Long) {
        viewModelScope.launch {
            taskRepository.toggleTaskCompletion(taskId)
            WidgetUpdateHelper.updateAllWidgetsAsync(appContext)
        }
    }

    fun createTask(title: String, description: String? = null, priority: Priority = Priority.MEDIUM, dueDate: Long? = null) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val task = Task(
                title = title.trim(),
                description = description?.trim()?.ifBlank { null },
                priority = priority,
                dueDate = dueDate
            )
            taskRepository.insertTask(task)
            WidgetUpdateHelper.updateAllWidgetsAsync(appContext)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskRepository.updateTask(task)
            _dialogState.update { it.copy(editingTask = null) }
            WidgetUpdateHelper.updateAllWidgetsAsync(appContext)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId)
            DaymarkAlarmReceiver.cancelTaskReminder(appContext, taskId)
            WidgetUpdateHelper.updateAllWidgetsAsync(appContext)
        }
    }

    fun restoreTask(task: Task) {
        viewModelScope.launch {
            taskRepository.insertTask(task)
            WidgetUpdateHelper.updateAllWidgetsAsync(appContext)
        }
    }

    fun duplicateTask(taskId: Long) {
        viewModelScope.launch {
            taskRepository.duplicateTask(taskId)
            WidgetUpdateHelper.updateAllWidgetsAsync(appContext)
        }
    }

    fun setTaskReminder(taskId: Long, triggerAtMillis: Long) {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(taskId) ?: return@launch
            val updated = task.copy(reminderTime = triggerAtMillis)
            taskRepository.updateTask(updated)
            DaymarkAlarmReceiver.scheduleTaskReminder(appContext, taskId, task.title, triggerAtMillis)
            _dialogState.update { it.copy(settingReminderForTask = null) }
        }
    }

    fun removeTaskReminder(taskId: Long) {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(taskId) ?: return@launch
            val updated = task.copy(reminderTime = null)
            taskRepository.updateTask(updated)
            DaymarkAlarmReceiver.cancelTaskReminder(appContext, taskId)
            _dialogState.update { it.copy(settingReminderForTask = null) }
        }
    }

    fun toggleCompletedSection() {
        _dialogState.update { it.copy(showCompleted = !it.showCompleted) }
    }

    fun openTimerForTask(task: Task) {
        _dialogState.update { it.copy(activeTaskForTimer = task) }
    }

    fun closeTimerSheet() {
        _dialogState.update { it.copy(activeTaskForTimer = null) }
    }

    fun openStandaloneTimerPicker() {
        _dialogState.update { it.copy(showStandaloneTimerPicker = true) }
    }

    fun closeStandaloneTimerPicker() {
        _dialogState.update { it.copy(showStandaloneTimerPicker = false) }
    }

    fun openMotivationSheet() {
        _dialogState.update { it.copy(showMotivationSheet = true) }
    }

    fun closeMotivationSheet() {
        _dialogState.update { it.copy(showMotivationSheet = false) }
    }

    fun setCustomVideoUri(uri: String?) {
        viewModelScope.launch {
            settingsRepository.setCustomVideoUri(uri)
        }
    }

    fun setVideoStartFromBeginning(start: Boolean) {
        viewModelScope.launch {
            settingsRepository.setVideoStartFromBeginning(start)
        }
    }

    fun setVideoResumePosition(pos: Long) {
        viewModelScope.launch {
            settingsRepository.setVideoResumePositionMs(pos)
        }
    }

    fun openEditTask(task: Task) {
        _dialogState.update { it.copy(editingTask = task) }
    }

    fun closeEditTask() {
        _dialogState.update { it.copy(editingTask = null) }
    }

    fun openReminderDialog(task: Task) {
        _dialogState.update { it.copy(settingReminderForTask = task) }
    }

    fun closeReminderDialog() {
        _dialogState.update { it.copy(settingReminderForTask = null) }
    }

    class Factory(
        private val taskRepository: TaskRepository,
        private val goalRepository: GoalRepository,
        private val settingsRepository: SettingsRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TodoViewModel(taskRepository, goalRepository, settingsRepository, context) as T
        }
    }
}
