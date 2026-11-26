package com.daymark.app.domain.models

enum class Priority {
    LOW,
    MEDIUM,
    HIGH
}

enum class TrackingType {
    CHECKBOX,
    MEASURABLE
}

enum class GoalStatus {
    ACTIVE,
    PAUSED,
    ARCHIVED
}

enum class TimerStatus {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED
}

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}
