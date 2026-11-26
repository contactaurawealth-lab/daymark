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
import com.daymark.app.core.utilities.DateTimeUtils
import com.daymark.app.domain.models.Goal
import kotlinx.coroutines.flow.first

data class GoalWidgetItem(
    val goal: Goal,
    val completedToday: Boolean,
    val progressToday: Double
)

class ToggleGoalActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val goalId = parameters[GoalIdKey] ?: return
        val container = (context.applicationContext as DaymarkApplication).container
        val todayCheckIn = container.goalRepository.getTodayCheckIn(goalId)
        val isCompleted = todayCheckIn?.completed == true

        container.goalRepository.checkInGoal(
            goalId = goalId,
            date = DateTimeUtils.todayEpochDay(),
            recordedValue = if (!isCompleted) 1.0 else 0.0,
            completed = !isCompleted
        )
        GoalProgressWidget().update(context, glanceId)
        WidgetUpdateHelper.updateAllWidgets(context)
    }

    companion object {
        val GoalIdKey = ActionParameters.Key<Long>("goal_id")
    }
}

class GoalProgressWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val container = (context.applicationContext as DaymarkApplication).container
        val activeGoals: List<GoalWidgetItem> = try {
            val goals = container.goalRepository.getActiveGoals().first()
            goals.map { g ->
                val checkIn = container.goalRepository.getTodayCheckIn(g.id)
                GoalWidgetItem(
                    goal = g,
                    completedToday = checkIn?.completed == true,
                    progressToday = checkIn?.recordedValue ?: 0.0
                )
            }
        } catch (e: Exception) {
            emptyList()
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
                // Header
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .clickable(actionStartActivity(openIntent)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = "Goals",
                            style = TextStyle(
                                color = textPri,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        val doneCount = activeGoals.count { it.completedToday }
                        Text(
                            text = if (activeGoals.isEmpty()) "Max 6 active" else "$doneCount of ${activeGoals.size} completed",
                            style = TextStyle(color = textSec, fontSize = 11.sp)
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(10.dp))

                if (activeGoals.isEmpty()) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .clickable(actionStartActivity(openIntent)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "🎯",
                                style = TextStyle(fontSize = 20.sp)
                            )
                            Spacer(modifier = GlanceModifier.height(4.dp))
                            Text(
                                text = "No active goals",
                                style = TextStyle(color = textSec, fontSize = 12.sp)
                            )
                        }
                    }
                } else {
                    LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                        items(activeGoals) { item ->
                            Row(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .cornerRadius(12.dp)
                                    .background(cardBg)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Interactive Checkmark: tap checks in directly
                                Box(
                                    modifier = GlanceModifier
                                        .size(22.dp)
                                        .cornerRadius(11.dp)
                                        .background(if (item.completedToday) accent else bg)
                                        .clickable(
                                            actionRunCallback<ToggleGoalActionCallback>(
                                                actionParametersOf(ToggleGoalActionCallback.GoalIdKey to item.goal.id)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (item.completedToday) "✓" else "○",
                                        style = TextStyle(
                                            color = if (item.completedToday) ColorProvider(Color.White) else textSec,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                Spacer(modifier = GlanceModifier.width(10.dp))

                                Text(
                                    text = item.goal.title,
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

class GoalProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GoalProgressWidget()
}
