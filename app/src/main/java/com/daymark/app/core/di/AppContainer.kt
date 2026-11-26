package com.daymark.app.core.di

import android.content.Context
import com.daymark.app.core.database.DaymarkDatabase
import com.daymark.app.data.repositories.BackupRepository
import com.daymark.app.data.repositories.GoalRepository
import com.daymark.app.data.repositories.SettingsRepository
import com.daymark.app.data.repositories.TaskRepository
import com.daymark.app.data.repositories.ThemeRepository
import com.daymark.app.data.repositories.TimerRepository

interface AppContainer {
    val database: DaymarkDatabase
    val taskRepository: TaskRepository
    val goalRepository: GoalRepository
    val timerRepository: TimerRepository
    val settingsRepository: SettingsRepository
    val themeRepository: ThemeRepository
    val backupRepository: BackupRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val database: DaymarkDatabase by lazy {
        DaymarkDatabase.getDatabase(context)
    }

    override val taskRepository: TaskRepository by lazy {
        TaskRepository(database.taskDao(), database.taskListDao())
    }

    override val goalRepository: GoalRepository by lazy {
        GoalRepository(database.goalDao(), database.goalCheckInDao())
    }

    override val timerRepository: TimerRepository by lazy {
        TimerRepository(context, database.timerDao(), database.taskDao())
    }

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(context)
    }

    override val themeRepository: ThemeRepository by lazy {
        ThemeRepository(database.customThemeDao(), settingsRepository)
    }

    override val backupRepository: BackupRepository by lazy {
        BackupRepository(database, settingsRepository)
    }
}
