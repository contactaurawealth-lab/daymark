package com.daymark.app.features.notifications

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.daymark.app.DaymarkApplication
import com.daymark.app.R
import com.daymark.app.core.utilities.NotificationHelper
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class DailySummaryWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as DaymarkApplication).container
        val settings = container.settingsRepository.settingsFlow.first()

        if (!settings.notificationsEnabled || !settings.dailySummaryEnabled) {
            return Result.success()
        }

        if (settings.quietHoursEnabled && NotificationHelper.isQuietHours(settings.quietHoursStart, settings.quietHoursEnd)) {
            return Result.success()
        }

        val completedTasks = container.taskRepository.getCompletedTasks().first().size
        val pendingTasks = container.taskRepository.getPendingTasks().first().size
        val activeGoals = container.goalRepository.getActiveGoals().first()

        var goalsCompletedToday = 0
        for (goal in activeGoals) {
            val checkIn = container.goalRepository.getTodayCheckIn(goal.id)
            if (checkIn?.completed == true) {
                goalsCompletedToday++
            }
        }

        val summaryText = "Today: $completedTasks tasks completed ($pendingTasks remaining), $goalsCompletedToday of ${activeGoals.size} goals achieved."

        val notification = NotificationCompat.Builder(applicationContext, NotificationHelper.CHANNEL_SUMMARY)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle("Daily Overview")
            .setContentText(summaryText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val nm = NotificationManagerCompat.from(applicationContext)
        try {
            nm.notify(3001, notification)
        } catch (e: SecurityException) {
            // Permission denied
        }

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "daymark_daily_summary_work"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<DailySummaryWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(calculateInitialDelayHours(), TimeUnit.HOURS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        private fun calculateInitialDelayHours(): Long {
            // Target ~20:00 (8:00 PM)
            val nowHour = java.time.LocalTime.now().hour
            return if (nowHour < 20) (20 - nowHour).toLong() else (24 - nowHour + 20).toLong()
        }
    }
}
