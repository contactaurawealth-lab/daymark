package com.daymark.app.core.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.daymark.app.domain.models.GoalCheckIn

@Entity(
    tableName = "goal_check_ins",
    indices = [
        Index(value = ["goalId", "localDate"], unique = true),
        Index(value = ["goalId"]),
        Index(value = ["localDate"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GoalCheckInEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val goalId: Long,
    val localDate: Long, // Epoch day
    val recordedValue: Double = 0.0,
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): GoalCheckIn = GoalCheckIn(
        id = id,
        goalId = goalId,
        localDate = localDate,
        recordedValue = recordedValue,
        completed = completed,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(checkIn: GoalCheckIn): GoalCheckInEntity = GoalCheckInEntity(
            id = checkIn.id,
            goalId = checkIn.goalId,
            localDate = checkIn.localDate,
            recordedValue = checkIn.recordedValue,
            completed = checkIn.completed,
            createdAt = checkIn.createdAt
        )
    }
}
