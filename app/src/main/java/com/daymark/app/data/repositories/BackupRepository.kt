package com.daymark.app.data.repositories

import com.daymark.app.core.database.DaymarkDatabase
import com.daymark.app.core.database.entities.CustomThemeEntity
import com.daymark.app.core.database.entities.GoalCheckInEntity
import com.daymark.app.core.database.entities.GoalEntity
import com.daymark.app.core.database.entities.TaskEntity
import com.daymark.app.core.database.entities.TaskListEntity
import com.daymark.app.core.database.entities.TimerSessionEntity
import com.daymark.app.domain.models.AppSettings
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

class BackupRepository(
    private val database: DaymarkDatabase,
    private val settingsRepository: SettingsRepository
) {
    suspend fun exportDataJson(): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("app", "Daymark")
        root.put("exportedAt", System.currentTimeMillis())

        // Tasks
        val tasks = database.taskDao().getAllTasks().first()
        val tasksArray = JSONArray()
        for (t in tasks) {
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("title", t.title)
            obj.put("description", t.description ?: "")
            obj.put("createdAt", t.createdAt)
            obj.put("dueDate", t.dueDate ?: -1L)
            obj.put("priority", t.priority.name)
            obj.put("completed", t.completed)
            obj.put("completedAt", t.completedAt ?: -1L)
            obj.put("listId", t.listId ?: -1L)
            obj.put("sortOrder", t.sortOrder)
            obj.put("reminderTime", t.reminderTime ?: -1L)
            tasksArray.put(obj)
        }
        root.put("tasks", tasksArray)

        // Goals
        val goals = database.goalDao().getAllGoals().first()
        val goalsArray = JSONArray()
        for (g in goals) {
            val obj = JSONObject()
            obj.put("id", g.id)
            obj.put("title", g.title)
            obj.put("description", g.description ?: "")
            obj.put("icon", g.icon)
            obj.put("color", g.color)
            obj.put("trackingType", g.trackingType.name)
            obj.put("targetValue", g.targetValue)
            obj.put("unit", g.unit)
            obj.put("startDate", g.startDate)
            obj.put("durationDays", g.durationDays)
            obj.put("reminderTime", g.reminderTime ?: "")
            obj.put("status", g.status.name)
            goalsArray.put(obj)
        }
        root.put("goals", goalsArray)

        // Goal Check-Ins
        val checkIns = database.goalCheckInDao().getAllCheckInsSync()
        val checkInsArray = JSONArray()
        for (c in checkIns) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("goalId", c.goalId)
            obj.put("localDate", c.localDate)
            obj.put("recordedValue", c.recordedValue)
            obj.put("completed", c.completed)
            obj.put("createdAt", c.createdAt)
            checkInsArray.put(obj)
        }
        root.put("goalCheckIns", checkInsArray)

        // Timer Sessions
        val sessions = database.timerDao().getAllSessionsSync()
        val sessionsArray = JSONArray()
        for (s in sessions) {
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("taskId", s.taskId)
            obj.put("durationSeconds", s.durationSeconds)
            obj.put("startedAt", s.startedAt)
            obj.put("elapsedSeconds", s.elapsedSeconds)
            obj.put("status", s.status.name)
            obj.put("completedAt", s.completedAt ?: -1L)
            sessionsArray.put(obj)
        }
        root.put("timerSessions", sessionsArray)

        // Custom Themes
        val themes = database.customThemeDao().getAllThemesSync()
        val themesArray = JSONArray()
        for (th in themes) {
            val obj = JSONObject()
            obj.put("id", th.id)
            obj.put("name", th.name)
            obj.put("primaryColor", th.primaryColor)
            obj.put("secondaryColor", th.secondaryColor)
            obj.put("accentColor", th.accentColor)
            obj.put("isDark", th.isDark)
            themesArray.put(obj)
        }
        root.put("customThemes", themesArray)

        // Settings
        val settings = settingsRepository.settingsFlow.first()
        val settingsObj = JSONObject()
        settingsObj.put("themeMode", settings.themeMode.name)
        settingsObj.put("activeThemeId", settings.activeThemeId)
        settingsObj.put("defaultTimerMinutes", settings.defaultTimerMinutes)
        settingsObj.put("notificationsEnabled", settings.notificationsEnabled)
        settingsObj.put("taskRemindersEnabled", settings.taskRemindersEnabled)
        settingsObj.put("goalRemindersEnabled", settings.goalRemindersEnabled)
        settingsObj.put("quietHoursEnabled", settings.quietHoursEnabled)
        root.put("settings", settingsObj)

        return root.toString(2)
    }

    suspend fun importDataJson(jsonString: String): Result<Int> {
        return try {
            val root = JSONObject(jsonString)
            if (!root.has("app") || root.getString("app") != "Daymark") {
                return Result.failure(IllegalArgumentException("Invalid Daymark backup file."))
            }

            var importedCount = 0

            // Import Tasks
            if (root.has("tasks")) {
                val tasksArray = root.getJSONArray("tasks")
                for (i in 0 until tasksArray.length()) {
                    val obj = tasksArray.getJSONObject(i)
                    val task = TaskEntity(
                        id = 0, // Fresh ID to prevent collision
                        title = obj.getString("title"),
                        description = obj.optString("description").takeIf { it.isNotEmpty() },
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        dueDate = obj.optLong("dueDate").takeIf { it > 0 },
                        priority = try {
                            com.daymark.app.domain.models.Priority.valueOf(obj.optString("priority", "MEDIUM"))
                        } catch (e: Exception) {
                            com.daymark.app.domain.models.Priority.MEDIUM
                        },
                        completed = obj.optBoolean("completed", false),
                        completedAt = obj.optLong("completedAt").takeIf { it > 0 },
                        sortOrder = obj.optInt("sortOrder", 0),
                        reminderTime = obj.optLong("reminderTime").takeIf { it > 0 }
                    )
                    database.taskDao().insertTask(task)
                    importedCount++
                }
            }

            // Import Custom Themes
            if (root.has("customThemes")) {
                val themesArray = root.getJSONArray("customThemes")
                for (i in 0 until themesArray.length()) {
                    val obj = themesArray.getJSONObject(i)
                    val theme = CustomThemeEntity(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        primaryColor = obj.getLong("primaryColor"),
                        secondaryColor = obj.getLong("secondaryColor"),
                        accentColor = obj.getLong("accentColor"),
                        isDark = obj.optBoolean("isDark", false)
                    )
                    database.customThemeDao().insertTheme(theme)
                    importedCount++
                }
            }

            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAllData() {
        database.taskDao().clearAll()
        database.taskListDao().clearAll()
        database.goalDao().clearAll()
        database.goalCheckInDao().clearAll()
        database.timerDao().clearAll()
        database.customThemeDao().clearAll()
    }
}
