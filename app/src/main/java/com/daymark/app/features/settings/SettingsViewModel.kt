package com.daymark.app.features.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.daymark.app.core.designsystem.ThemePresets
import com.daymark.app.data.repositories.BackupRepository
import com.daymark.app.data.repositories.SettingsRepository
import com.daymark.app.data.repositories.ThemeRepository
import com.daymark.app.domain.models.AppSettings
import com.daymark.app.domain.models.AppTheme
import com.daymark.app.domain.models.ThemeMode
import com.daymark.app.features.notifications.DailySummaryWorker
import com.daymark.app.features.widgets.WidgetUpdateHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsDialogState(
    val isExporting: Boolean = false,
    val exportJsonContent: String? = null,
    val importSuccessMessage: String? = null,
    val errorMessage: String? = null
)

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val themes: List<AppTheme> = ThemePresets.allPresets,
    val activeTheme: AppTheme = ThemePresets.Daylight,
    val isExporting: Boolean = false,
    val exportJsonContent: String? = null,
    val importSuccessMessage: String? = null,
    val errorMessage: String? = null
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val themeRepository: ThemeRepository,
    private val backupRepository: BackupRepository,
    private val appContext: Context
) : ViewModel() {

    private val _dialogState = MutableStateFlow(SettingsDialogState())

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.settingsFlow,
        themeRepository.allThemes,
        themeRepository.activeTheme,
        _dialogState
    ) { settings, themes, activeTheme, dialog ->
        SettingsUiState(
            settings = settings,
            themes = themes,
            activeTheme = activeTheme,
            isExporting = dialog.isExporting,
            exportJsonContent = dialog.exportJsonContent,
            importSuccessMessage = dialog.importSuccessMessage,
            errorMessage = dialog.errorMessage
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun selectTheme(themeId: String) {
        viewModelScope.launch {
            themeRepository.setActiveTheme(themeId)
            WidgetUpdateHelper.updateAllWidgets(appContext)
        }
    }

    fun saveCustomTheme(name: String, primaryColor: Long, secondaryColor: Long, accentColor: Long, isDark: Boolean) {
        viewModelScope.launch {
            val id = "custom_" + System.currentTimeMillis()
            val theme = AppTheme(
                id = id,
                name = name.ifBlank { "Custom Theme" },
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                accentColor = accentColor,
                isDark = isDark,
                isCustom = true
            )
            themeRepository.saveCustomTheme(theme)
            WidgetUpdateHelper.updateAllWidgets(appContext)
        }
    }

    fun deleteCustomTheme(themeId: String) {
        viewModelScope.launch {
            themeRepository.deleteCustomTheme(themeId)
            WidgetUpdateHelper.updateAllWidgets(appContext)
        }
    }

    fun resetThemeToDefault() {
        viewModelScope.launch {
            themeRepository.resetToDefault()
            WidgetUpdateHelper.updateAllWidgets(appContext)
        }
    }

    fun setDefaultTimerMinutes(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setDefaultTimerMinutes(minutes)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
        }
    }

    fun setTaskRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setTaskRemindersEnabled(enabled)
        }
    }

    fun setGoalRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setGoalRemindersEnabled(enabled)
        }
    }

    fun setTimerCompletionNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setTimerCompletionNotificationEnabled(enabled)
        }
    }

    fun setDailySummaryEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDailySummaryEnabled(enabled)
            if (enabled) {
                DailySummaryWorker.schedule(appContext)
            } else {
                DailySummaryWorker.cancel(appContext)
            }
        }
    }

    fun setQuietHoursEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setQuietHoursEnabled(enabled)
        }
    }

    fun setQuietHours(start: String, end: String) {
        viewModelScope.launch {
            settingsRepository.setQuietHours(start, end)
        }
    }

    fun setDailyMotivationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDailyMotivationEnabled(enabled)
        }
    }

    fun setMorningGreetingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setMorningGreetingEnabled(enabled)
        }
    }

    fun setEveningReflectionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setEveningReflectionEnabled(enabled)
        }
    }

    fun setCustomVideoUri(uri: String?) {
        viewModelScope.launch {
            settingsRepository.setCustomVideoUri(uri)
        }
    }

    fun setVideoStartFromBeginning(startFromBeginning: Boolean) {
        viewModelScope.launch {
            settingsRepository.setVideoStartFromBeginning(startFromBeginning)
        }
    }

    fun setAmbientSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAmbientSoundEnabled(enabled)
        }
    }

    fun exportBackup() {
        viewModelScope.launch {
            _dialogState.update { it.copy(isExporting = true) }
            try {
                val json = backupRepository.exportDataJson()
                _dialogState.update { it.copy(exportJsonContent = json, isExporting = false) }
            } catch (e: Exception) {
                _dialogState.update { it.copy(errorMessage = "Export failed: ${e.message}", isExporting = false) }
            }
        }
    }

    fun clearExportContent() {
        _dialogState.update { it.copy(exportJsonContent = null) }
    }

    fun importBackup(jsonString: String) {
        viewModelScope.launch {
            val result = backupRepository.importDataJson(jsonString)
            if (result.isSuccess) {
                _dialogState.update { it.copy(importSuccessMessage = "Successfully imported ${result.getOrNull()} items!") }
                WidgetUpdateHelper.updateAllWidgets(appContext)
            } else {
                _dialogState.update { it.copy(errorMessage = "Import failed: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            backupRepository.clearAllData()
            WidgetUpdateHelper.updateAllWidgets(appContext)
        }
    }

    fun clearMessages() {
        _dialogState.update { it.copy(importSuccessMessage = null, errorMessage = null) }
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
        private val themeRepository: ThemeRepository,
        private val backupRepository: BackupRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(settingsRepository, themeRepository, backupRepository, context) as T
        }
    }
}
