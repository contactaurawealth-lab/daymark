package com.daymark.app.core.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.daymark.app.domain.models.Goal
import com.daymark.app.domain.models.GoalStatus
import com.daymark.app.domain.models.TrackingType

@Entity(
    tableName = "goals",
    indices = [Index(value = ["status"])]
)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val icon: String = "sparkles",
    val color: Long = 0xFF91A68A,
    val trackingType: TrackingType = TrackingType.CHECKBOX,
    val targetValue: Double = 1.0,
    val unit: String = "times",
    val startDate: Long,
    val durationDays: Int = 30,
    val reminderTime: String? = null,
    val status: GoalStatus = GoalStatus.ACTIVE
) {
    fun toDomain(): Goal = Goal(
        id = id,
        title = title,
        description = description,
        icon = icon,
        color = color,
        trackingType = trackingType,
        targetValue = targetValue,
        unit = unit,
        startDate = startDate,
        durationDays = durationDays,
        reminderTime = reminderTime,
        status = status
    )

    companion object {
        fun fromDomain(goal: Goal): GoalEntity = GoalEntity(
            id = goal.id,
            title = goal.title,
            description = goal.description,
            icon = goal.icon,
            color = goal.color,
            trackingType = goal.trackingType,
            targetValue = goal.targetValue,
            unit = goal.unit,
            startDate = goal.startDate,
            durationDays = goal.durationDays,
            reminderTime = goal.reminderTime,
            status = goal.status
        )
    }
}
