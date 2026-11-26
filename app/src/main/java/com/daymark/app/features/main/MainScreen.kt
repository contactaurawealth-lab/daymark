package com.daymark.app.features.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.daymark.app.core.designsystem.LocalDaymarkColors
import com.daymark.app.domain.models.Task
import com.daymark.app.features.goals.GoalsScreen
import com.daymark.app.features.goals.GoalsViewModel
import com.daymark.app.features.main.components.DaymarkTopBar
import com.daymark.app.features.settings.SettingsBottomSheet
import com.daymark.app.features.settings.SettingsViewModel
import com.daymark.app.features.timer.TimerBottomSheet
import com.daymark.app.features.timer.TimerViewModel
import com.daymark.app.features.todo.TodoScreen
import com.daymark.app.features.todo.TodoViewModel

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    todoViewModel: TodoViewModel,
    goalsViewModel: GoalsViewModel,
    timerViewModel: TimerViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val colors = LocalDaymarkColors.current
    var currentTab by rememberSaveable { mutableStateOf(MainTab.TODO) }
    var showSettingsSheet by rememberSaveable { mutableStateOf(false) }
    var showTimerSheet by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        containerColor = colors.primaryBackground,
        topBar = {
            DaymarkTopBar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
                onOpenSettings = { showSettingsSheet = true }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal Animated Sliding Content with subtle fade (280ms duration)
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    if (targetState == MainTab.GOALS) {
                        // Slide left when navigating Todo -> Goals
                        (slideInHorizontally(
                            animationSpec = tween(280, easing = FastOutSlowInEasing)
                        ) { width -> width / 3 } + fadeIn(animationSpec = tween(280)))
                            .togetherWith(
                                slideOutHorizontally(
                                    animationSpec = tween(280, easing = FastOutSlowInEasing)
                                ) { width -> -width / 3 } + fadeOut(animationSpec = tween(200))
                            )
                    } else {
                        // Slide right when navigating Goals -> Todo
                        (slideInHorizontally(
                            animationSpec = tween(280, easing = FastOutSlowInEasing)
                        ) { width -> -width / 3 } + fadeIn(animationSpec = tween(280)))
                            .togetherWith(
                                slideOutHorizontally(
                                    animationSpec = tween(280, easing = FastOutSlowInEasing)
                                ) { width -> width / 3 } + fadeOut(animationSpec = tween(200))
                            )
                    }
                },
                label = "main_tab_transition"
            ) { tab ->
                when (tab) {
                    MainTab.TODO -> {
                        TodoScreen(
                            viewModel = todoViewModel,
                            onStartTimerForTask = { task, minutes ->
                                timerViewModel.startTimer(task, minutes)
                                showTimerSheet = true
                            }
                        )
                    }
                    MainTab.GOALS -> {
                        GoalsScreen(
                            viewModel = goalsViewModel
                        )
                    }
                }
            }
        }
    }

    // Settings Bottom Sheet
    if (showSettingsSheet) {
        SettingsBottomSheet(
            viewModel = settingsViewModel,
            onDismiss = { showSettingsSheet = false }
        )
    }

    // Active Timer Bottom Sheet
    if (showTimerSheet) {
        TimerBottomSheet(
            viewModel = timerViewModel,
            onDismiss = { showTimerSheet = false }
        )
    }
}
