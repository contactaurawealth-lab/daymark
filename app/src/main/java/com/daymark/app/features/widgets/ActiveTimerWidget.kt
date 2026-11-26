package com.daymark.app.features.widgets

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.daymark.app.DaymarkApplication
import com.daymark.app.MainActivity
import com.daymark.app.core.utilities.DateTimeUtils
import com.daymark.app.data.repositories.PersistedTimerState
import com.daymark.app.domain.models.TimerStatus
import com.daymark.app.features.timer.service.TimerForegroundService
import kotlinx.coroutines.flow.first

class TimerControlActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val actionType = parameters[TimerActionKey] ?: return
        when (actionType) {
            "pause" -> TimerForegroundService.pause(context)
            "resume" -> TimerForegroundService.resume(context)
            "cancel" -> TimerForegroundService.cancel(context)
        }
        ActiveTimerWidget().update(context, glanceId)
        WidgetUpdateHelper.updateAllWidgets(context)
    }

    companion object {
        val TimerActionKey = ActionParameters.Key<String>("timer_action")
    }
}

class ActiveTimerWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val container = (context.applicationContext as DaymarkApplication).container
        val timerState = try {
            container.timerRepository.persistedState.first()
        } catch (e: Exception) {
            PersistedTimerState()
        }

        val theme = try {
            container.themeRepository.activeTheme.first()
        } catch (e: Exception) {
            null
        }

        provideContent {
            val bgCol = Color(theme?.primaryColor ?: 0xFFFBFBF9)
            val cardCol = Color(theme?.secondaryColor ?: 0xFFF0EFEB)
            val accentCol = Color(theme?.accentColor ?: 0xFF91A68A)
            val isDark = theme?.isDark ?: false
            val textPriCol = if (isDark) Color(0xFFF1F1F1) else Color(0xFF2B303A)
            val textSecCol = if (isDark) Color(0xFFA0A3A9) else Color(0xFF71747C)

            val bg = ColorProvider(bgCol)
            val cardBg = ColorProvider(cardCol)
            val accent = ColorProvider(accentCol)
            val textPri = ColorProvider(textPriCol)
            val textSec = ColorProvider(textSecCol)

            val remainingSec = when (timerState.status) {
                TimerStatus.RUNNING -> {
                    val target = timerState.targetEndTime ?: 0L
                    val now = System.currentTimeMillis()
                    if (target > now) (target - now) / 1000 else 0L
                }
                TimerStatus.PAUSED -> {
                    (timerState.remainingMillisOnPause ?: 0L) / 1000
                }
                else -> timerState.totalDurationSeconds
            }

            val timeDisplay = DateTimeUtils.formatSecondsToTime(remainingSec)
            val statusDisplay = when (timerState.status) {
                TimerStatus.RUNNING -> "Focusing"
                TimerStatus.PAUSED -> "Paused"
                TimerStatus.COMPLETED -> "Finished"
                TimerStatus.IDLE -> "Focus Timer"
            }

            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(24.dp)
                    .background(bg)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Task Title / Status
                Text(
                    text = timerState.taskTitle.ifBlank { statusDisplay },
                    maxLines = 1,
                    modifier = GlanceModifier.clickable(actionStartActivity(openIntent)),
                    style = TextStyle(
                        color = textSec,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = GlanceModifier.height(4.dp))

                // Time Display
                Text(
                    text = timeDisplay,
                    modifier = GlanceModifier.clickable(actionStartActivity(openIntent)),
                    style = TextStyle(
                        color = if (timerState.status == TimerStatus.RUNNING) accent else textPri,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Interactive Controls
                if (timerState.status == TimerStatus.RUNNING || timerState.status == TimerStatus.PAUSED) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pause / Resume Button
                        val isPaused = timerState.status == TimerStatus.PAUSED
                        val actionKey = if (isPaused) "resume" else "pause"
                        Box(
                            modifier = GlanceModifier
                                .size(34.dp)
                                .cornerRadius(17.dp)
                                .background(accent)
                                .clickable(
                                    actionRunCallback<TimerControlActionCallback>(
                                        actionParametersOf(TimerControlActionCallback.TimerActionKey to actionKey)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isPaused) "▶" else "❚❚",
                                style = TextStyle(
                                    color = ColorProvider(Color.White),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(12.dp))

                        // Cancel / Stop Button
                        Box(
                            modifier = GlanceModifier
                                .size(34.dp)
                                .cornerRadius(17.dp)
                                .background(cardBg)
                                .clickable(
                                    actionRunCallback<TimerControlActionCallback>(
                                        actionParametersOf(TimerControlActionCallback.TimerActionKey to "cancel")
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✕",
                                style = TextStyle(
                                    color = textSec,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                } else {
                    // Tap to start focus session
                    Box(
                        modifier = GlanceModifier
                            .cornerRadius(12.dp)
                            .background(cardBg)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .clickable(actionStartActivity(openIntent)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Start Focus",
                            style = TextStyle(
                                color = accent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

class ActiveTimerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ActiveTimerWidget()
}
