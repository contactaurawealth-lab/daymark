package com.daymark.app.core.database

import androidx.room.TypeConverter
import com.daymark.app.domain.models.GoalStatus
import com.daymark.app.domain.models.Priority
import com.daymark.app.domain.models.TimerStatus
import com.daymark.app.domain.models.TrackingType

class Converters {
    @TypeConverter
    fun fromPriority(priority: Priority): String = priority.name

    @TypeConverter
    fun toPriority(value: String): Priority = try {
        Priority.valueOf(value)
    } catch (e: Exception) {
        Priority.MEDIUM
    }

    @TypeConverter
    fun fromTrackingType(type: TrackingType): String = type.name

    @TypeConverter
    fun toTrackingType(value: String): TrackingType = try {
        TrackingType.valueOf(value)
    } catch (e: Exception) {
        TrackingType.CHECKBOX
    }

    @TypeConverter
    fun fromGoalStatus(status: GoalStatus): String = status.name

    @TypeConverter
    fun toGoalStatus(value: String): GoalStatus = try {
        GoalStatus.valueOf(value)
    } catch (e: Exception) {
        GoalStatus.ACTIVE
    }

    @TypeConverter
    fun fromTimerStatus(status: TimerStatus): String = status.name

    @TypeConverter
    fun toTimerStatus(value: String): TimerStatus = try {
        TimerStatus.valueOf(value)
    } catch (e: Exception) {
        TimerStatus.IDLE
    }
}
