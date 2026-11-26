package com.daymark.app.data.repositories

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.daymark.app.domain.models.AppSettings
import com.daymark.app.domain.models.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "daymark_settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACTIVE_THEME_ID = stringPreferencesKey("active_theme_id")
        val DEFAULT_TIMER_MINUTES = intPreferencesKey("default_timer_minutes")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val TASK_REMINDERS_ENABLED = booleanPreferencesKey("task_reminders_enabled")
        val GOAL_REMINDERS_ENABLED = booleanPreferencesKey("goal_reminders_enabled")
        val TIMER_COMPLETION_NOTI = booleanPreferencesKey("timer_completion_noti")
        val DAILY_SUMMARY_ENABLED = booleanPreferencesKey("daily_summary_enabled")
        val DAILY_SUMMARY_TIME = stringPreferencesKey("daily_summary_time")
        val QUIET_HOURS_ENABLED = booleanPreferencesKey("quiet_hours_enabled")
        val QUIET_HOURS_START = stringPreferencesKey("quiet_hours_start")
        val QUIET_HOURS_END = stringPreferencesKey("quiet_hours_end")

        // New Feature Preferences
        val DAILY_MOTIVATION_ENABLED = booleanPreferencesKey("daily_motivation_enabled")
        val MORNING_GREETING_ENABLED = booleanPreferencesKey("morning_greeting_enabled")
        val EVENING_REFLECTION_ENABLED = booleanPreferencesKey("evening_reflection_enabled")
        val CUSTOM_VIDEO_URI = stringPreferencesKey("custom_video_uri")
        val VIDEO_START_FROM_BEG = booleanPreferencesKey("video_start_from_beginning")
        val VIDEO_RESUME_POS = longPreferencesKey("video_resume_position_ms")
        val AMBIENT_SOUND_ENABLED = booleanPreferencesKey("ambient_sound_enabled")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            themeMode = try {
                ThemeMode.valueOf(prefs[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name)
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            },
            activeThemeId = prefs[PreferencesKeys.ACTIVE_THEME_ID] ?: "daylight",
            defaultTimerMinutes = prefs[PreferencesKeys.DEFAULT_TIMER_MINUTES] ?: 25,
            notificationsEnabled = prefs[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
            taskRemindersEnabled = prefs[PreferencesKeys.TASK_REMINDERS_ENABLED] ?: true,
            goalRemindersEnabled = prefs[PreferencesKeys.GOAL_REMINDERS_ENABLED] ?: true,
            timerCompletionNotificationEnabled = prefs[PreferencesKeys.TIMER_COMPLETION_NOTI] ?: true,
            dailySummaryEnabled = prefs[PreferencesKeys.DAILY_SUMMARY_ENABLED] ?: false,
            dailySummaryTime = prefs[PreferencesKeys.DAILY_SUMMARY_TIME] ?: "20:00",
            quietHoursEnabled = prefs[PreferencesKeys.QUIET_HOURS_ENABLED] ?: false,
            quietHoursStart = prefs[PreferencesKeys.QUIET_HOURS_START] ?: "22:00",
            quietHoursEnd = prefs[PreferencesKeys.QUIET_HOURS_END] ?: "07:00",

            dailyMotivationEnabled = prefs[PreferencesKeys.DAILY_MOTIVATION_ENABLED] ?: true,
            morningGreetingEnabled = prefs[PreferencesKeys.MORNING_GREETING_ENABLED] ?: true,
            eveningReflectionEnabled = prefs[PreferencesKeys.EVENING_REFLECTION_ENABLED] ?: true,
            customVideoUri = prefs[PreferencesKeys.CUSTOM_VIDEO_URI],
            videoStartFromBeginning = prefs[PreferencesKeys.VIDEO_START_FROM_BEG] ?: true,
            videoResumePositionMs = prefs[PreferencesKeys.VIDEO_RESUME_POS] ?: 0L,
            ambientSoundEnabled = prefs[PreferencesKeys.AMBIENT_SOUND_ENABLED] ?: false
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode.name }
    }

    suspend fun setActiveThemeId(themeId: String) {
        context.dataStore.edit { it[PreferencesKeys.ACTIVE_THEME_ID] = themeId }
    }

    suspend fun setDefaultTimerMinutes(minutes: Int) {
        context.dataStore.edit { it[PreferencesKeys.DEFAULT_TIMER_MINUTES] = minutes }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setTaskRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.TASK_REMINDERS_ENABLED] = enabled }
    }

    suspend fun setGoalRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.GOAL_REMINDERS_ENABLED] = enabled }
    }

    suspend fun setTimerCompletionNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.TIMER_COMPLETION_NOTI] = enabled }
    }

    suspend fun setDailySummaryEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DAILY_SUMMARY_ENABLED] = enabled }
    }

    suspend fun setDailySummaryTime(time: String) {
        context.dataStore.edit { it[PreferencesKeys.DAILY_SUMMARY_TIME] = time }
    }

    suspend fun setQuietHoursEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.QUIET_HOURS_ENABLED] = enabled }
    }

    suspend fun setQuietHours(start: String, end: String) {
        context.dataStore.edit {
            it[PreferencesKeys.QUIET_HOURS_START] = start
            it[PreferencesKeys.QUIET_HOURS_END] = end
        }
    }

    suspend fun setDailyMotivationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DAILY_MOTIVATION_ENABLED] = enabled }
    }

    suspend fun setMorningGreetingEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.MORNING_GREETING_ENABLED] = enabled }
    }

    suspend fun setEveningReflectionEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.EVENING_REFLECTION_ENABLED] = enabled }
    }

    suspend fun setCustomVideoUri(uri: String?) {
        context.dataStore.edit {
            if (uri != null) {
                it[PreferencesKeys.CUSTOM_VIDEO_URI] = uri
            } else {
                it.remove(PreferencesKeys.CUSTOM_VIDEO_URI)
            }
        }
    }

    suspend fun setVideoStartFromBeginning(startFromBeginning: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.VIDEO_START_FROM_BEG] = startFromBeginning }
    }

    suspend fun setVideoResumePositionMs(positionMs: Long) {
        context.dataStore.edit { it[PreferencesKeys.VIDEO_RESUME_POS] = positionMs }
    }

    suspend fun setAmbientSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.AMBIENT_SOUND_ENABLED] = enabled }
    }
}
