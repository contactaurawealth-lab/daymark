package com.daymark.app.features.todo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daymark.app.core.designsystem.LocalDaymarkColors
import com.daymark.app.domain.models.Task
import com.daymark.app.features.motivation.MotivationSheet
import com.daymark.app.features.todo.components.TaskEditDialog
import com.daymark.app.features.todo.components.TaskItemRow
import com.daymark.app.features.todo.components.TaskReminderDialog
import com.daymark.app.features.todo.components.TaskTimerPickerSheet

@Composable
fun TodoScreen(
    viewModel: TodoViewModel,
    onStartTimerForTask: (Task?, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalDaymarkColors.current
    var newTaskTitle by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.primaryBackground)
            .padding(horizontal = 20.dp)
    ) {
        // Date Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = uiState.currentDateFormatted,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            if (uiState.totalCount > 0) {
                Text(
                    text = "${uiState.completedCount} of ${uiState.totalCount} completed",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = colors.textSecondary,
                        fontSize = 13.sp
                    )
                )
            }
        }

        // Daily Progress Indicator Card
        val progress = uiState.dailyProgress
        val progressPercent = (progress.overallCompletionRate).toInt().coerceIn(0, 100)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.secondaryContainer.copy(alpha = 0.6f))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Progress",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary,
                            fontSize = 12.sp
                        )
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "$progressPercent%",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            fontSize = 13.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { (progress.overallCompletionRate / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = colors.accent,
                    trackColor = colors.primaryBackground,
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${progress.completedTasksToday}/${progress.totalTasksToday} tasks today",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    )
                    if (progress.totalActiveGoals > 0) {
                        Text(
                            text = "${progress.completedGoalsToday}/${progress.totalActiveGoals} goals today",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = colors.textSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Motivation & Focus Mode Quick Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Motivation Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.secondaryContainer.copy(alpha = 0.5f))
                    .clickable { viewModel.openMotivationSheet() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (uiState.appSettings.customVideoUri != null) Icons.Outlined.PlayCircle else Icons.Outlined.AutoAwesome,
                        contentDescription = "Daily Motivation",
                        tint = colors.accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (uiState.appSettings.customVideoUri != null) "Video Motivation" else "Daily Motivation",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        maxLines = 1
                    )
                }
            }

            // Standalone Focus Mode Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.secondaryContainer.copy(alpha = 0.5f))
                    .clickable { viewModel.openStandaloneTimerPicker() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.HourglassEmpty,
                        contentDescription = "Focus Mode",
                        tint = colors.accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Focus Mode",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        // Quick Add Task Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newTaskTitle,
                onValueChange = { newTaskTitle = it },
                placeholder = { Text("Add a task...", color = colors.textSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.secondaryContainer.copy(alpha = 0.5f),
                    unfocusedContainerColor = colors.secondaryContainer.copy(alpha = 0.5f),
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (newTaskTitle.isNotBlank()) {
                        viewModel.createTask(newTaskTitle)
                        newTaskTitle = ""
                    }
                }),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (newTaskTitle.isNotBlank()) {
                        viewModel.createTask(newTaskTitle)
                        newTaskTitle = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colors.accent)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Task List & Completed Section
        if (uiState.pendingTasks.isEmpty() && uiState.completedTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(colors.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✓", fontSize = 24.sp, color = colors.accent, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "All clear",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Take a breath or add a task above.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(uiState.pendingTasks, key = { it.id }) { task ->
                    TaskItemRow(
                        task = task,
                        onToggleCompletion = { viewModel.toggleTaskCompletion(task.id) },
                        onClick = { viewModel.openEditTask(task) },
                        onStartTimer = { viewModel.openTimerForTask(task) },
                        onEdit = { viewModel.openEditTask(task) },
                        onSetReminder = { viewModel.openReminderDialog(task) },
                        onDuplicate = { viewModel.duplicateTask(task.id) },
                        onDelete = { viewModel.deleteTask(task.id) }
                    )
                }

                if (uiState.completedTasks.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.toggleCompletedSection() }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Completed (${uiState.completedTasks.size})",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = colors.textSecondary,
                                    fontSize = 13.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (uiState.showCompletedSection) {
                                    Icons.Outlined.KeyboardArrowUp
                                } else {
                                    Icons.Outlined.KeyboardArrowDown
                                },
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (uiState.showCompletedSection) {
                        items(uiState.completedTasks, key = { it.id }) { task ->
                            TaskItemRow(
                                task = task,
                                onToggleCompletion = { viewModel.toggleTaskCompletion(task.id) },
                                onClick = { viewModel.openEditTask(task) },
                                onStartTimer = { viewModel.openTimerForTask(task) },
                                onEdit = { viewModel.openEditTask(task) },
                                onSetReminder = { viewModel.openReminderDialog(task) },
                                onDuplicate = { viewModel.duplicateTask(task.id) },
                                onDelete = { viewModel.deleteTask(task.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Sheets & Dialogs
    // Task-specific Timer Picker
    uiState.activeTaskForTimer?.let { task ->
        TaskTimerPickerSheet(
            task = task,
            defaultMinutes = uiState.appSettings.defaultTimerMinutes,
            onStartTimer = { minutes ->
                viewModel.closeTimerSheet()
                onStartTimerForTask(task, minutes)
            },
            onDismiss = { viewModel.closeTimerSheet() }
        )
    }

    // Standalone Focus Session Timer Picker
    if (uiState.showStandaloneTimerPicker) {
        TaskTimerPickerSheet(
            task = null,
            defaultMinutes = uiState.appSettings.defaultTimerMinutes,
            onStartTimer = { minutes ->
                viewModel.closeStandaloneTimerPicker()
                onStartTimerForTask(null, minutes)
            },
            onDismiss = { viewModel.closeStandaloneTimerPicker() }
        )
    }

    // Daily Motivation Bottom Sheet (Quotes, Greeting, Personal Video Player)
    if (uiState.showMotivationSheet) {
        MotivationSheet(
            settings = uiState.appSettings,
            onSaveVideoUri = { viewModel.setCustomVideoUri(it) },
            onSetStartFromBeginning = { viewModel.setVideoStartFromBeginning(it) },
            onSaveResumePosition = { viewModel.setVideoResumePosition(it) },
            onDismiss = { viewModel.closeMotivationSheet() }
        )
    }

    uiState.editingTask?.let { task ->
        TaskEditDialog(
            task = task,
            onSave = { updated -> viewModel.updateTask(updated) },
            onDismiss = { viewModel.closeEditTask() }
        )
    }

    uiState.settingReminderForTask?.let { task ->
        TaskReminderDialog(
            task = task,
            onSetReminder = { time -> viewModel.setTaskReminder(task.id, time) },
            onRemoveReminder = { viewModel.removeTaskReminder(task.id) },
            onDismiss = { viewModel.closeReminderDialog() }
        )
    }
}
