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
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
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
import com.daymark.app.domain.models.Task
import kotlinx.coroutines.flow.first

class ToggleTaskActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val taskId = parameters[TaskIdKey] ?: return
        val container = (context.applicationContext as DaymarkApplication).container
        container.taskRepository.toggleTaskCompletion(taskId)
        TodayTasksWidget().update(context, glanceId)
        WidgetUpdateHelper.updateAllWidgets(context)
    }

    companion object {
        val TaskIdKey = ActionParameters.Key<Long>("task_id")
    }
}

class TodayTasksWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val container = (context.applicationContext as DaymarkApplication).container
        val pendingTasks = try {
            container.taskRepository.getPendingTasks().first().take(8)
        } catch (e: Exception) {
            emptyList<Task>()
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

            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(24.dp)
                    .background(bg)
                    .padding(14.dp)
            ) {
                // Header with title, counter, and quick-add shortcut
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .clickable(actionStartActivity(openIntent))
                    ) {
                        Text(
                            text = "Today",
                            style = TextStyle(
                                color = textPri,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = if (pendingTasks.isEmpty()) "All clear" else "${pendingTasks.size} remaining",
                            style = TextStyle(color = textSec, fontSize = 11.sp)
                        )
                    }

                    // Quick-add button
                    Box(
                        modifier = GlanceModifier
                            .size(28.dp)
                            .cornerRadius(14.dp)
                            .background(cardBg)
                            .clickable(actionStartActivity(openIntent)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            style = TextStyle(
                                color = accent,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(10.dp))

                // Task list or calm empty state
                if (pendingTasks.isEmpty()) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .clickable(actionStartActivity(openIntent)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "✓",
                                style = TextStyle(
                                    color = accent,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = GlanceModifier.height(4.dp))
                            Text(
                                text = "All clear for today",
                                style = TextStyle(color = textSec, fontSize = 12.sp)
                            )
                        }
                    }
                } else {
                    LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                        items(pendingTasks) { task ->
                            Row(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .cornerRadius(12.dp)
                                    .background(cardBg)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Interactive Checkbox: tapping checks off task directly
                                Box(
                                    modifier = GlanceModifier
                                        .size(20.dp)
                                        .cornerRadius(10.dp)
                                        .background(bg)
                                        .clickable(
                                            actionRunCallback<ToggleTaskActionCallback>(
                                                actionParametersOf(ToggleTaskActionCallback.TaskIdKey to task.id)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "○",
                                        style = TextStyle(
                                            color = accent,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                Spacer(modifier = GlanceModifier.width(10.dp))

                                // Task title: tapping opens Daymark
                                Text(
                                    text = task.title,
                                    maxLines = 1,
                                    modifier = GlanceModifier
                                        .defaultWeight()
                                        .clickable(actionStartActivity(openIntent)),
                                    style = TextStyle(
                                        color = textPri,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                            Spacer(modifier = GlanceModifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}

class TodayTasksWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayTasksWidget()
}
