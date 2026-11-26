package com.daymark.app.core.utilities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.daymark.app.MainActivity
import com.daymark.app.R
import java.time.LocalTime

object NotificationHelper {
    const val CHANNEL_TASKS = "daymark_task_reminders"
    const val CHANNEL_GOALS = "daymark_goal_reminders"
    const val CHANNEL_TIMER_RUNNING = "daymark_timer_running"
    const val CHANNEL_TIMER_COMPLETE = "daymark_timer_complete"
    const val CHANNEL_SUMMARY = "daymark_daily_summary"

    const val NOTIFICATION_TIMER_FG_ID = 2001
    const val NOTIFICATION_TIMER_DONE_ID = 2002

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val tasksChannel = NotificationChannel(
                CHANNEL_TASKS,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for scheduled tasks"
                enableVibration(true)
            }

            val goalsChannel = NotificationChannel(
                CHANNEL_GOALS,
                "Goal Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminders for active goals"
            }

            val timerRunningChannel = NotificationChannel(
                CHANNEL_TIMER_RUNNING,
                "Active Countdown Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Displays live task countdown status"
                setShowBadge(false)
            }

            val timerDoneChannel = NotificationChannel(
                CHANNEL_TIMER_COMPLETE,
                "Timer Completion Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when a task countdown timer has completed"
                enableVibration(true)
            }

            val summaryChannel = NotificationChannel(
                CHANNEL_SUMMARY,
                "Daily Productivity Summary",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "End-of-day task and goal progress summary"
            }

            nm.createNotificationChannels(
                listOf(tasksChannel, goalsChannel, timerRunningChannel, timerDoneChannel, summaryChannel)
            )
        }
    }

    fun isQuietHours(startStr: String, endStr: String): Boolean {
        try {
            val now = LocalTime.now()
            val start = LocalTime.parse(startStr)
            val end = LocalTime.parse(endStr)
            return if (start.isBefore(end)) {
                now.isAfter(start) && now.isBefore(end)
            } else {
                now.isAfter(start) || now.isBefore(end)
            }
        } catch (e: Exception) {
            return false
        }
    }

    fun showTimerCompleteNotification(context: Context, taskTitle: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_TIMER_COMPLETE)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle("Timer Complete!")
            .setContentText("Focus session for \"$taskTitle\" is complete.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        val nm = NotificationManagerCompat.from(context)
        try {
            nm.notify(NOTIFICATION_TIMER_DONE_ID, notification)
        } catch (e: SecurityException) {
            // Android 13+ permission not granted
        }
    }
}
