package com.daymark.app.features.widgets

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object WidgetUpdateHelper {
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pendingUpdateJob: Job? = null

    /**
     * Non-blocking, debounced widget refresh for interactive UI actions so rapid clicks
     * update the screen immediately without waiting for AppWidgetManager IPC.
     */
    fun updateAllWidgetsAsync(context: Context) {
        val appContext = context.applicationContext
        pendingUpdateJob?.cancel()
        pendingUpdateJob = backgroundScope.launch {
            delay(150L)
            updateAllWidgets(appContext)
        }
    }

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
