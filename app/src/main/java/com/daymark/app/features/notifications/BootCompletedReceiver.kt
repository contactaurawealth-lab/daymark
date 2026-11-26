package com.daymark.app.features.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.daymark.app.DaymarkApplication
import com.daymark.app.domain.models.TimerStatus
import com.daymark.app.features.timer.service.TimerForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                val container = (context.applicationContext as DaymarkApplication).container

                // 1. Reschedule uncompleted task reminders
                val pendingTasks = container.taskRepository.getPendingTasks().first()
                val now = System.currentTimeMillis()
                for (task in pendingTasks) {
                    val reminderTime = task.reminderTime
                    if (reminderTime != null && reminderTime > now) {
                        DaymarkAlarmReceiver.scheduleTaskReminder(context, task.id, task.title, reminderTime)
                    }
                }

                // 2. Check if a timer was running before reboot and expired
                val timerState = container.timerRepository.persistedState.first()
                if (timerState.status == TimerStatus.RUNNING) {
                    val targetEnd = timerState.targetEndTime ?: 0L
                    if (targetEnd in 1..now) {
                        // Timer completed while device was powered down
                        val taskId = timerState.taskId ?: 0L
                        container.timerRepository.recordSession(
                            taskId = taskId,
                            durationSeconds = timerState.totalDurationSeconds,
                            elapsedSeconds = timerState.totalDurationSeconds,
                            status = TimerStatus.COMPLETED
                        )
                        container.timerRepository.clearTimerState()
                        com.daymark.app.core.utilities.NotificationHelper.showTimerCompleteNotification(
                            context, timerState.taskTitle
                        )
                    } else if (targetEnd > now) {
                        // Timer still has time left: resume foreground service
                        val remainingSec = (targetEnd - now) / 1000
                        val taskId = timerState.taskId ?: 0L
                        TimerForegroundService.start(context, taskId, timerState.taskTitle, remainingSec)
                    }
                }
            }
        }
    }
}
