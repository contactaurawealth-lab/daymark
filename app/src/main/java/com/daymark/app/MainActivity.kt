package com.daymark.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.daymark.app.core.designsystem.DaymarkTheme
import com.daymark.app.features.goals.GoalsViewModel
import com.daymark.app.features.main.MainScreen
import com.daymark.app.features.main.MainViewModel
import com.daymark.app.features.settings.SettingsViewModel
import com.daymark.app.features.timer.TimerViewModel
import com.daymark.app.features.todo.TodoViewModel

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Graceful permission handling
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Android 13+ Notification Permission check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val appContainer = (application as DaymarkApplication).container

        val mainViewModel: MainViewModel by viewModels {
            MainViewModel.Factory(appContainer.themeRepository, appContainer.settingsRepository)
        }
        val todoViewModel: TodoViewModel by viewModels {
            TodoViewModel.Factory(
                appContainer.taskRepository,
                appContainer.goalRepository,
                appContainer.settingsRepository,
                applicationContext
            )
        }
        val goalsViewModel: GoalsViewModel by viewModels {
            GoalsViewModel.Factory(appContainer.goalRepository, applicationContext)
        }
        val timerViewModel: TimerViewModel by viewModels {
            TimerViewModel.Factory(appContainer.timerRepository, applicationContext)
        }
        val settingsViewModel: SettingsViewModel by viewModels {
            SettingsViewModel.Factory(
                appContainer.settingsRepository,
                appContainer.themeRepository,
                appContainer.backupRepository,
                applicationContext
            )
        }

        setContent {
            val activeTheme by mainViewModel.activeTheme.collectAsState()
            val themeMode by mainViewModel.themeMode.collectAsState()

            DaymarkTheme(
                appTheme = activeTheme,
                themeMode = themeMode
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(
                        mainViewModel = mainViewModel,
                        todoViewModel = todoViewModel,
                        goalsViewModel = goalsViewModel,
                        timerViewModel = timerViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}
