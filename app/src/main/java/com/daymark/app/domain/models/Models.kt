package com.daymark.app.domain.models

data class Task(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val dueDate: Long? = null,
    val priority: Priority = Priority.MEDIUM,
    val completed: Boolean = false,
    val completedAt: Long? = null,
    val listId: Long? = null,
    val sortOrder: Int = 0,
    val reminderTime: Long? = null
)

data class TaskList(
    val id: Long = 0,
    val name: String,
    val sortOrder: Int = 0
)

data class Goal(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val icon: String = "sparkles",
    val color: Long = 0xFF91A68A,
    val trackingType: TrackingType = TrackingType.CHECKBOX,
    val targetValue: Double = 1.0,
    val unit: String = "times",
    val startDate: Long, // Epoch day
    val durationDays: Int = 30,
    val reminderTime: String? = null, // e.g. "09:00"
    val status: GoalStatus = GoalStatus.ACTIVE
)

data class GoalCheckIn(
    val id: Long = 0,
    val goalId: Long,
    val localDate: Long, // Epoch day
    val recordedValue: Double = 0.0,
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class TimerSession(
    val id: Long = 0,
    val taskId: Long,
    val taskTitle: String = "",
    val durationSeconds: Long,
    val startedAt: Long,
    val elapsedSeconds: Long,
    val status: TimerStatus,
    val completedAt: Long? = null
)

data class StreakInfo(
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val completedDays: Int = 0,
    val missedDays: Int = 0,
    val totalScheduledDays: Int = 0,
    val completionRate: Float = 0f,
    val remainingDays: Int = 0,
    val todayCompleted: Boolean = false,
    val todayProgress: Double = 0.0
)

data class DailyProgress(
    val completedTasksToday: Int = 0,
    val totalTasksToday: Int = 0,
    val taskCompletionRate: Float = 0f,
    val completedGoalsToday: Int = 0,
    val totalActiveGoals: Int = 0,
    val goalCompletionRate: Float = 0f,
    val overallCompletionRate: Float = 0f
)

data class MotivationalMessage(
    val quote: String,
    val author: String,
    val timeOfDayGreeting: String,
    val reflectionPrompt: String
)

data class AppTheme(
    val id: String,
    val name: String,
    val primaryColor: Long,   // 60% background/surface
    val secondaryColor: Long, // 30% container/surfaceVariant
    val accentColor: Long,    // 10% accent
    val isDark: Boolean = false,
    val isCustom: Boolean = false
)

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val activeThemeId: String = "daylight",
    val defaultTimerMinutes: Int = 25,
    val notificationsEnabled: Boolean = true,
    val taskRemindersEnabled: Boolean = true,
    val goalRemindersEnabled: Boolean = true,
    val timerCompletionNotificationEnabled: Boolean = true,
    val dailySummaryEnabled: Boolean = false,
    val dailySummaryTime: String = "20:00",
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "07:00",
    // New Feature Settings
    val dailyMotivationEnabled: Boolean = true,
    val morningGreetingEnabled: Boolean = true,
    val eveningReflectionEnabled: Boolean = true,
    val customVideoUri: String? = null,
    val videoStartFromBeginning: Boolean = true,
    val videoResumePositionMs: Long = 0L,
    val ambientSoundEnabled: Boolean = false
)
