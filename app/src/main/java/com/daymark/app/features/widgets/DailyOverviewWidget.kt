package com.daymark.app.features.widgets

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
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
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.daymark.app.DaymarkApplication
import com.daymark.app.MainActivity
import com.daymark.app.domain.usecases.DailyProgressCalculator
import kotlinx.coroutines.flow.first

class DailyOverviewWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val container = (context.applicationContext as DaymarkApplication).container

        val progress = try {
            val allTasks = container.taskRepository.getAllTasks().first()
            val activeGoals = container.goalRepository.getActiveGoals().first()
            val todayCheckIns = activeGoals.mapNotNull { g ->
                container.goalRepository.getTodayCheckIn(g.id)
            }
            DailyProgressCalculator.calculate(allTasks, activeGoals, todayCheckIns)
        } catch (e: Exception) {
            null
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
                    .clickable(actionStartActivity(openIntent))
            ) {
                // Header
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daymark",
                        style = TextStyle(
                            color = textPri,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    val pct = (progress?.overallCompletionRate ?: 0f).toInt()
                    Text(
                        text = "$pct%",
                        style = TextStyle(
                            color = accent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(10.dp))

                // Dual Overview Cards
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tasks Card
                    Column(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .cornerRadius(12.dp)
                            .background(cardBg)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Tasks",
                            style = TextStyle(color = textSec, fontSize = 11.sp)
                        )
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        val doneTasks = progress?.completedTasksToday ?: 0
                        val totalTasks = progress?.totalTasksToday ?: 0
                        Text(
                            text = "$doneTasks / $totalTasks",
                            style = TextStyle(
                                color = textPri,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(8.dp))

                    // Goals Card
                    Column(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .cornerRadius(12.dp)
                            .background(cardBg)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Goals",
                            style = TextStyle(color = textSec, fontSize = 11.sp)
                        )
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        val doneGoals = progress?.completedGoalsToday ?: 0
                        val totalGoals = progress?.totalActiveGoals ?: 0
                        Text(
                            text = "$doneGoals / $totalGoals",
                            style = TextStyle(
                                color = textPri,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

class DailyOverviewWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DailyOverviewWidget()
}
