package com.daymark.app.data.repositories

import com.daymark.app.core.database.dao.CustomThemeDao
import com.daymark.app.core.database.entities.CustomThemeEntity
import com.daymark.app.core.designsystem.ThemePresets
import com.daymark.app.domain.models.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class ThemeRepository(
    private val customThemeDao: CustomThemeDao,
    private val settingsRepository: SettingsRepository
) {
    val allThemes: Flow<List<AppTheme>> =
        customThemeDao.getAllThemes().map { customEntities ->
            val custom = customEntities.map { it.toDomain() }
            ThemePresets.allPresets + custom
        }

    val activeTheme: Flow<AppTheme> =
        combine(allThemes, settingsRepository.settingsFlow) { themes, settings ->
            themes.find { it.id == settings.activeThemeId } ?: ThemePresets.Daylight
        }

    suspend fun saveCustomTheme(theme: AppTheme) {
        customThemeDao.insertTheme(CustomThemeEntity.fromDomain(theme))
        settingsRepository.setActiveThemeId(theme.id)
    }

    suspend fun deleteCustomTheme(id: String) {
        customThemeDao.deleteTheme(id)
        settingsRepository.setActiveThemeId(ThemePresets.Daylight.id)
    }

    suspend fun setActiveTheme(id: String) {
        settingsRepository.setActiveThemeId(id)
    }

    suspend fun resetToDefault() {
        settingsRepository.setActiveThemeId(ThemePresets.Daylight.id)
    }
}
