package com.daymark.app.features.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.daymark.app.data.repositories.SettingsRepository
import com.daymark.app.data.repositories.ThemeRepository
import com.daymark.app.domain.models.AppTheme
import com.daymark.app.domain.models.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

enum class MainTab {
    TODO,
    GOALS
}

class MainViewModel(
    private val themeRepository: ThemeRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val activeTheme: StateFlow<AppTheme> = themeRepository.activeTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.daymark.app.core.designsystem.ThemePresets.Daylight)

    val themeMode: StateFlow<ThemeMode> = settingsRepository.settingsFlow
        .map { it.themeMode }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    class Factory(
        private val themeRepository: ThemeRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(themeRepository, settingsRepository) as T
        }
    }
}
