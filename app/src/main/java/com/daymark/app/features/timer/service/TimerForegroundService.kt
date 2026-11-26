package com.daymark.app.features.timer.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.daymark.app.DaymarkApplication
import com.daymark.app.MainActivity
import com.daymark.app.R
import com.daymark.app.core.utilities.AmbientSoundPlayer
import com.daymark.app.core.utilities.DateTimeUtils
import com.daymark.app.core.utilities.NotificationHelper
import com.daymark.app.data.repositories.PersistedTimerState
import com.daymark.app.domain.models.TimerStatus
import com.daymark.app.features.widgets.WidgetUpdateHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var tickerJob: Job? = null

    private var currentTaskId: Long = 0
    private var currentTaskTitle: String = ""
    private var totalDurationSeconds: Long = 1500L
    private var elapsedSeconds: Long = 0
    private var isPaused: Boolean = false

    // Monotonic clock baseline for countdown precision
    private var monotonicStartTime: Long = 0
    private var accumulatedElapsedBeforeResume: Long = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val taskId = intent.getLongExtra(EXTRA_TASK_ID, 0L)
                val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task"
                val durationSec = intent.getLongExtra(EXTRA_DURATION_SEC, 1500L)
                startTimer(taskId, taskTitle, durationSec)
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_CANCEL -> cancelTimer()
        }
        return START_STICKY
    }

    private fun startTimer(taskId: Long, taskTitle: String, durationSec: Long) {
        currentTaskId = taskId
        currentTaskTitle = taskTitle
        totalDurationSeconds = durationSec
        accumulatedElapsedBeforeResume = 0
        monotonicStartTime = SystemClock.elapsedRealtime()
        isPaused = false

        startForeground(NotificationHelper.NOTIFICATION_TIMER_FG_ID, buildNotification())
        persistState(TimerStatus.RUNNING)
        startTicker()

        serviceScope.launch {
            try {
                val container = (applicationContext as DaymarkApplication).container
                val soundEnabled = container.settingsRepository.settingsFlow.first().ambientSoundEnabled
                if (soundEnabled) {
                    AmbientSoundPlayer.start()
                }
            } catch (e: Exception) {
                // Safeguard
            }
        }
    }

    private fun pauseTimer() {
        if (isPaused) return
        isPaused = true
        accumulatedElapsedBeforeResume += (SystemClock.elapsedRealtime() - monotonicStartTime) / 1000
        tickerJob?.cancel()
        AmbientSoundPlayer.pause()
        updateNotification()
        persistState(TimerStatus.PAUSED)
    }

    private fun resumeTimer() {
        if (!isPaused) return
        isPaused = false
        monotonicStartTime = SystemClock.elapsedRealtime()
        persistState(TimerStatus.RUNNING)
        startTicker()

        serviceScope.launch {
            try {
                val container = (applicationContext as DaymarkApplication).container
                val soundEnabled = container.settingsRepository.settingsFlow.first().ambientSoundEnabled
                if (soundEnabled) {
                    AmbientSoundPlayer.resume()
                }
            } catch (e: Exception) {
                // Safeguard
            }
        }
    }

    private fun cancelTimer() {
        tickerJob?.cancel()
        AmbientSoundPlayer.stop()
        serviceScope.launch {
            try {
                val container = (applicationContext as DaymarkApplication).container
                container.timerRepository.recordSession(
                    taskId = currentTaskId,
                    durationSeconds = totalDurationSeconds,
                    elapsedSeconds = elapsedSeconds,
                    status = TimerStatus.IDLE
                )
                container.timerRepository.clearTimerState()
                WidgetUpdateHelper.updateAllWidgets(applicationContext)
            } catch (e: Exception) {
                // Safeguard
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun completeTimer() {
        tickerJob?.cancel()
        AmbientSoundPlayer.stop()
        serviceScope.launch {
            try {
                val container = (applicationContext as DaymarkApplication).container
                container.timerRepository.recordSession(
                    taskId = currentTaskId,
                    durationSeconds = totalDurationSeconds,
                    elapsedSeconds = totalDurationSeconds,
                    status = TimerStatus.COMPLETED
                )
                container.timerRepository.clearTimerState()
                NotificationHelper.showTimerCompleteNotification(applicationContext, currentTaskTitle)
                WidgetUpdateHelper.updateAllWidgets(applicationContext)
            } catch (e: Exception) {
                // Safeguard
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            while (isActive) {
                val deltaSec = (SystemClock.elapsedRealtime() - monotonicStartTime) / 1000
                elapsedSeconds = accumulatedElapsedBeforeResume + deltaSec

                if (elapsedSeconds >= totalDurationSeconds) {
                    elapsedSeconds = totalDurationSeconds
                    completeTimer()
                    break
                } else {
                    updateNotification()
                    delay(1000)
                }
            }
        }
    }

    private fun persistState(status: TimerStatus) {
        serviceScope.launch {
            try {
                val container = (applicationContext as DaymarkApplication).container
                val remainingSec = totalDurationSeconds - elapsedSeconds
                val targetEnd = if (status == TimerStatus.RUNNING) {
                    System.currentTimeMillis() + (remainingSec * 1000)
                } else null

                container.timerRepository.saveTimerState(
                    PersistedTimerState(
                        taskId = currentTaskId,
                        taskTitle = currentTaskTitle,
                        targetEndTime = targetEnd,
                        remainingMillisOnPause = if (status == TimerStatus.PAUSED) remainingSec * 1000 else null,
                        totalDurationSeconds = totalDurationSeconds,
                        status = status
                    )
                )
                WidgetUpdateHelper.updateAllWidgets(applicationContext)
            } catch (e: Exception) {
                // Safeguard
            }
        }
    }

    private fun updateNotification() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.notify(NotificationHelper.NOTIFICATION_TIMER_FG_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val remainingSec = if (totalDurationSeconds > elapsedSeconds) totalDurationSeconds - elapsedSeconds else 0L
        val formattedTime = DateTimeUtils.formatSecondsToTime(remainingSec)
        val statusText = if (isPaused) "Paused • $formattedTime" else "Focusing • $formattedTime"

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseResumeAction = if (isPaused) {
            val resumeIntent = Intent(this, TimerForegroundService::class.java).apply { action = ACTION_RESUME }
            val resumePendingIntent = PendingIntent.getService(
                this, 1, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            NotificationCompat.Action.Builder(0, "Resume", resumePendingIntent).build()
        } else {
            val pauseIntent = Intent(this, TimerForegroundService::class.java).apply { action = ACTION_PAUSE }
            val pausePendingIntent = PendingIntent.getService(
                this, 2, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            NotificationCompat.Action.Builder(0, "Pause", pausePendingIntent).build()
        }

        val cancelIntent = Intent(this, TimerForegroundService::class.java).apply { action = ACTION_CANCEL }
        val cancelPendingIntent = PendingIntent.getService(
            this, 3, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val cancelAction = NotificationCompat.Action.Builder(0, "Cancel", cancelPendingIntent).build()

        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_TIMER_RUNNING)
            .setSmallIcon(R.drawable.ic_stat_timer)
            .setContentTitle(currentTaskTitle.ifBlank { "Task Timer" })
            .setContentText(statusText)
            .setOngoing(!isPaused)
            .setContentIntent(openAppPendingIntent)
            .addAction(pauseResumeAction)
            .addAction(cancelAction)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .build()
    }

    override fun onDestroy() {
        tickerJob?.cancel()
        AmbientSoundPlayer.stop()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.daymark.action.TIMER_START"
        const val ACTION_PAUSE = "com.daymark.action.TIMER_PAUSE"
        const val ACTION_RESUME = "com.daymark.action.TIMER_RESUME"
        const val ACTION_CANCEL = "com.daymark.action.TIMER_CANCEL"

        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_DURATION_SEC = "extra_duration_sec"

        fun start(context: Context, taskId: Long, taskTitle: String, durationSec: Long) {
            val intent = Intent(context, TimerForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TASK_ID, taskId)
                putExtra(EXTRA_TASK_TITLE, taskTitle)
                putExtra(EXTRA_DURATION_SEC, durationSec)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pause(context: Context) {
            context.startService(Intent(context, TimerForegroundService::class.java).apply { action = ACTION_PAUSE })
        }

        fun resume(context: Context) {
            context.startService(Intent(context, TimerForegroundService::class.java).apply { action = ACTION_RESUME })
        }

        fun cancel(context: Context) {
            context.startService(Intent(context, TimerForegroundService::class.java).apply { action = ACTION_CANCEL })
        }
    }
}
