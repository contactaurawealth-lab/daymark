package com.daymark.app.features.timer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.daymark.app.data.repositories.PersistedTimerState
import com.daymark.app.data.repositories.TimerRepository
import com.daymark.app.domain.models.Task
import com.daymark.app.domain.models.TimerStatus
import com.daymark.app.features.timer.service.TimerForegroundService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class TimerViewModel(
    private val timerRepository: TimerRepository,
    private val appContext: Context
) : ViewModel() {

    private val tickerFlow = kotlinx.coroutines.flow.flow {
        while (true) {
            emit(System.currentTimeMillis())
            kotlinx.coroutines.delay(1000L)
        }
    }

    val timerState: StateFlow<PersistedTimerState> = timerRepository.persistedState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PersistedTimerState())

    val remainingSeconds: StateFlow<Long> = kotlinx.coroutines.flow.combine(
        timerState,
        tickerFlow
    ) { state, now ->
        when (state.status) {
            TimerStatus.RUNNING -> {
                val target = state.targetEndTime ?: 0L
                if (target > now) (target - now) / 1000 else 0L
            }
            TimerStatus.PAUSED -> {
                (state.remainingMillisOnPause ?: 0L) / 1000
            }
            else -> state.totalDurationSeconds
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1500L)

    fun startTimer(task: Task?, durationMinutes: Int) {
        val durationSec = durationMinutes.toLong() * 60L
        TimerForegroundService.start(
            appContext,
            task?.id ?: 0L,
            task?.title ?: "Focus Session",
            durationSec
        )
    }

    fun pauseTimer() {
        TimerForegroundService.pause(appContext)
    }

    fun resumeTimer() {
        TimerForegroundService.resume(appContext)
    }

    fun cancelTimer() {
        TimerForegroundService.cancel(appContext)
    }

    class Factory(
        private val timerRepository: TimerRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TimerViewModel(timerRepository, context) as T
        }
    }
}
