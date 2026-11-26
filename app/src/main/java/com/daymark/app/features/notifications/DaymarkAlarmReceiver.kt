package com.daymark.app.features.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.daymark.app.DaymarkApplication
import com.daymark.app.MainActivity
import com.daymark.app.R
import com.daymark.app.core.utilities.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DaymarkAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val id = intent.getLongExtra(EXTRA_ID, 0L)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Daymark Reminder"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "You have a scheduled reminder."

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val container = (context.applicationContext as DaymarkApplication).container
            val settings = container.settingsRepository.settingsFlow.first()

            if (!settings.notificationsEnabled) return@launch
            if (settings.quietHoursEnabled && NotificationHelper.isQuietHours(settings.quietHoursStart, settings.quietHoursEnd)) {
                return@launch
            }

            when (action) {
                ACTION_TASK_REMINDER -> {
                    if (settings.taskRemindersEnabled) {
                        showReminderNotification(context, id.toInt(), title, message, NotificationHelper.CHANNEL_TASKS)
                    }
                }
                ACTION_GOAL_REMINDER -> {
                    if (settings.goalRemindersEnabled) {
                        showReminderNotification(context, (id + 10000).toInt(), title, message, NotificationHelper.CHANNEL_GOALS)
                    }
                }
                ACTION_SNOOZE -> {
                    // Snooze for 10 minutes
                    val snoozeTime = System.currentTimeMillis() + (10 * 60 * 1000)
                    scheduleTaskReminder(context, id, title, snoozeTime)
                    val nm = NotificationManagerCompat.from(context)
                    nm.cancel(id.toInt())
                }
            }
        }
    }

    private fun showReminderNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        channelId: String
    ) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpen = PendingIntent.getActivity(
            context, notificationId, openIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze Action (+10 min)
        val snoozeIntent = Intent(context, DaymarkAlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_ID, notificationId.toLong())
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_MESSAGE, message)
        }
        val pendingSnooze = PendingIntent.getBroadcast(
            context, notificationId + 50000, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingOpen)
            .addAction(0, "Snooze 10m", pendingSnooze)

        val nm = NotificationManagerCompat.from(context)
        try {
            nm.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Permission denied
        }
    }

    companion object {
        const val ACTION_TASK_REMINDER = "com.daymark.action.TASK_REMINDER"
        const val ACTION_GOAL_REMINDER = "com.daymark.action.GOAL_REMINDER"
        const val ACTION_SNOOZE = "com.daymark.action.SNOOZE"

        const val EXTRA_ID = "extra_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"

        fun scheduleTaskReminder(context: Context, taskId: Long, title: String, triggerAtMillis: Long) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, DaymarkAlarmReceiver::class.java).apply {
                action = ACTION_TASK_REMINDER
                putExtra(EXTRA_ID, taskId)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_MESSAGE, "Task reminder: $title")
            }
            val pending = PendingIntent.getBroadcast(
                context, taskId.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
                }
            } catch (e: SecurityException) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
            }
        }

        fun cancelTaskReminder(context: Context, taskId: Long) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, DaymarkAlarmReceiver::class.java).apply {
                action = ACTION_TASK_REMINDER
                putExtra(EXTRA_ID, taskId)
            }
            val pending = PendingIntent.getBroadcast(
                context, taskId.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pending)
        }
    }
}
