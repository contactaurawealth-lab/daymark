package com.daymark.app.core.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.daymark.app.domain.models.TimerSession
import com.daymark.app.domain.models.TimerStatus

@Entity(
    tableName = "timer_sessions",
    indices = [Index(value = ["taskId"])],
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TimerSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long,
    val durationSeconds: Long,
    val startedAt: Long,
    val elapsedSeconds: Long,
    val status: TimerStatus,
    val completedAt: Long? = null
) {
    fun toDomain(taskTitle: String = ""): TimerSession = TimerSession(
        id = id,
        taskId = taskId,
        taskTitle = taskTitle,
        durationSeconds = durationSeconds,
        startedAt = startedAt,
        elapsedSeconds = elapsedSeconds,
        status = status,
        completedAt = completedAt
    )

    companion object {
        fun fromDomain(session: TimerSession): TimerSessionEntity = TimerSessionEntity(
            id = session.id,
            taskId = session.taskId,
            durationSeconds = session.durationSeconds,
            startedAt = session.startedAt,
            elapsedSeconds = session.elapsedSeconds,
            status = session.status,
            completedAt = session.completedAt
        )
    }
}
