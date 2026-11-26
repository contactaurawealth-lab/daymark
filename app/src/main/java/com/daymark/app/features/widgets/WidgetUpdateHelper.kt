package com.daymark.app.features.widgets

import android.content.Context
import androidx.glance.appwidget.updateAll

object WidgetUpdateHelper {
    suspend fun updateAllWidgets(context: Context) {
        try {
            TodayTasksWidget().updateAll(context)
            GoalProgressWidget().updateAll(context)
            ActiveTimerWidget().updateAll(context)
            DailyOverviewWidget().updateAll(context)
        } catch (e: Exception) {
            // Widget update safeguard
        }
    }
}
