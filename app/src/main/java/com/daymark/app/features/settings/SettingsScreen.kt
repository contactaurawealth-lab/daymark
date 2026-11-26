package com.daymark.app.features.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daymark.app.core.designsystem.LocalDaymarkColors
import com.daymark.app.domain.models.AppTheme
import com.daymark.app.domain.models.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    viewModel: SettingsViewModel,
    onDismiss: () -> Unit
) {
    val colors = LocalDaymarkColors.current
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showCustomThemeDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showClearDataConfirm by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.primaryBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close", tint = colors.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 1: APPEARANCE & 60:30:10 THEMES
            SectionHeader(title = "Appearance & Themes")

            // Theme Mode (Light / Dark / System)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    ThemeMode.SYSTEM to "System",
                    ThemeMode.LIGHT to "Light",
                    ThemeMode.DARK to "Dark"
                ).forEach { (mode, label) ->
                    val isSelected = uiState.settings.themeMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) colors.accent else colors.secondaryContainer)
                            .clickable { viewModel.setThemeMode(mode) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isSelected) Color.White else colors.textPrimary,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Built-in Themes Grid
            Text(
                text = "Color Themes (60:30:10)",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 12.sp, color = colors.textSecondary)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.themes.forEach { theme ->
                    val isSelected = uiState.activeTheme.id == theme.id
                    ThemeRow(
                        theme = theme,
                        isSelected = isSelected,
                        onSelect = { viewModel.selectTheme(theme.id) },
                        onDelete = if (theme.isCustom) {
                            { viewModel.deleteCustomTheme(theme.id) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Custom Theme & Reset Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showCustomThemeDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.secondaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.Palette, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Custom Theme", color = colors.textPrimary, fontSize = 12.sp)
                }

                Button(
                    onClick = { viewModel.resetThemeToDefault() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.secondaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reset Theme", color = colors.textPrimary, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = colors.divider)
            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 2: NOTIFICATIONS
            SectionHeader(title = "Notifications")

            ToggleRow(
                title = "Enable Notifications",
                subtitle = "Master switch for all notifications",
                checked = uiState.settings.notificationsEnabled,
                onCheckedChange = { viewModel.setNotificationsEnabled(it) }
            )

            ToggleRow(
                title = "Task Reminders",
                subtitle = "Alert at scheduled task time",
                checked = uiState.settings.taskRemindersEnabled,
                onCheckedChange = { viewModel.setTaskRemindersEnabled(it) }
            )

            ToggleRow(
                title = "Goal Reminders",
                subtitle = "Daily nudges for active goals",
                checked = uiState.settings.goalRemindersEnabled,
                onCheckedChange = { viewModel.setGoalRemindersEnabled(it) }
            )

            ToggleRow(
                title = "Timer Completion Alerts",
                subtitle = "Vibrate and sound when focus session finishes",
                checked = uiState.settings.timerCompletionNotificationEnabled,
                onCheckedChange = { viewModel.setTimerCompletionNotificationEnabled(it) }
            )

            ToggleRow(
                title = "Quiet Hours",
                subtitle = "Silence reminders between 10:00 PM and 7:00 AM",
                checked = uiState.settings.quietHoursEnabled,
                onCheckedChange = { viewModel.setQuietHoursEnabled(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = colors.divider)
            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 2.5: MOTIVATION & FOCUS MODE
            SectionHeader(title = "Motivation & Focus")

            ToggleRow(
                title = "Daily Motivation",
                subtitle = "Daily inspiring messages and reflection prompts",
                checked = uiState.settings.dailyMotivationEnabled,
                onCheckedChange = { viewModel.setDailyMotivationEnabled(it) }
            )

            ToggleRow(
                title = "Morning Greeting",
                subtitle = "Show gentle greeting based on time of day",
                checked = uiState.settings.morningGreetingEnabled,
                onCheckedChange = { viewModel.setMorningGreetingEnabled(it) }
            )

            ToggleRow(
                title = "Evening Reflection",
                subtitle = "Evening prompts for mindful reflection",
                checked = uiState.settings.eveningReflectionEnabled,
                onCheckedChange = { viewModel.setEveningReflectionEnabled(it) }
            )

            ToggleRow(
                title = "Focus Ambient Sound",
                subtitle = "Play subtle calming acoustic noise during focus sessions",
                checked = uiState.settings.ambientSoundEnabled,
                onCheckedChange = { viewModel.setAmbientSoundEnabled(it) }
            )

            ToggleRow(
                title = "Start Video from Beginning",
                subtitle = "Play motivation video from start instead of resuming",
                checked = uiState.settings.videoStartFromBeginning,
                onCheckedChange = { viewModel.setVideoStartFromBeginning(it) }
            )

            if (uiState.settings.customVideoUri != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.setCustomVideoUri(null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.secondaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = colors.accent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Remove Motivation Video", color = colors.textPrimary, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = colors.divider)
            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 3: DEFAULT TIMER DURATION
            SectionHeader(title = "Default Focus Duration")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(15, 25, 45, 60).forEach { mins ->
                    val isSel = uiState.settings.defaultTimerMinutes == mins
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) colors.accent else colors.secondaryContainer)
                            .clickable { viewModel.setDefaultTimerMinutes(mins) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$mins m",
                            color = if (isSel) Color.White else colors.textPrimary,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = colors.divider)
            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 4: DATA BACKUP & PRIVACY
            SectionHeader(title = "Data & Privacy (Local-First)")
            Text(
                text = "All data stays exclusively on your device. Export as JSON anytime for offline backups.",
                style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        viewModel.exportBackup()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.secondaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.Download, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export JSON", color = colors.textPrimary, fontSize = 12.sp)
                }

                Button(
                    onClick = { showImportDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.secondaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.Upload, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import JSON", color = colors.textPrimary, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { showClearDataConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDE8E8)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Clear All Data", color = Color(0xFFE53935), fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = colors.divider)
            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 5: ABOUT
            SectionHeader(title = "About Daymark")
            Text(
                text = "Daymark v1.0.0",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Your day, clearly marked.\n100% offline, zero tracking, zero accounts.",
                style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary)
            )
        }
    }

    // Export Result Dialog
    uiState.exportJsonContent?.let { json ->
        AlertDialog(
            onDismissRequest = { viewModel.clearExportContent() },
            containerColor = colors.primaryBackground,
            title = { Text("Backup Ready", color = colors.textPrimary) },
            text = {
                Column {
                    Text("Your backup JSON has been generated.", color = colors.textSecondary)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = json.take(300) + if (json.length > 300) "..." else "",
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(json))
                        Toast.makeText(context, "Copied backup JSON to clipboard!", Toast.LENGTH_SHORT).show()
                        viewModel.clearExportContent()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                ) {
                    Text("Copy JSON", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearExportContent() }) {
                    Text("Close", color = colors.textSecondary)
                }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = colors.primaryBackground,
            title = { Text("Import Backup JSON", color = colors.textPrimary) },
            text = {
                Column {
                    Text("Paste your Daymark backup JSON below:", color = colors.textSecondary)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        placeholder = { Text("{ \"version\": 1, ... }") },
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJsonText.isNotBlank()) {
                            viewModel.importBackup(importJsonText)
                            showImportDialog = false
                            importJsonText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                ) {
                    Text("Import", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        )
    }

    // Clear Data Confirm Dialog
    if (showClearDataConfirm) {
        AlertDialog(
            onDismissRequest = { showClearDataConfirm = false },
            containerColor = colors.primaryBackground,
            title = { Text("Delete All Data?", color = Color(0xFFE53935)) },
            text = {
                Text("This action will permanently delete all tasks, goals, check-ins, and timers. It cannot be undone.", color = colors.textSecondary)
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDataConfirm = false
                        Toast.makeText(context, "All data wiped.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Wipe All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataConfirm = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        )
    }

    // Custom Theme Builder Dialog
    if (showCustomThemeDialog) {
        CustomThemeBuilderDialog(
            onSave = { name, pri, sec, acc, dark ->
                viewModel.saveCustomTheme(name, pri, sec, acc, dark)
                showCustomThemeDialog = false
            },
            onDismiss = { showCustomThemeDialog = false }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    val colors = LocalDaymarkColors.current
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary
        ),
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

@Composable
fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = LocalDaymarkColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(color = colors.textPrimary))
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary))
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colors.accent
            )
        )
    }
}

@Composable
fun ThemeRow(
    theme: AppTheme,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val colors = LocalDaymarkColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) colors.accent.copy(alpha = 0.15f) else colors.secondaryContainer.copy(alpha = 0.5f))
            .clickable(onClick = onSelect)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 60:30:10 Swatches
        Row(modifier = Modifier.width(60.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(theme.primaryColor))
            )
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(theme.secondaryColor))
            )
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(theme.accentColor))
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = theme.name,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = colors.textPrimary
            ),
            modifier = Modifier.weight(1f)
        )

        if (isSelected) {
            Icon(Icons.Outlined.Check, contentDescription = "Active Theme", tint = colors.accent, modifier = Modifier.size(18.dp))
        }

        if (onDelete != null) {
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete Theme", tint = Color(0xFFE53935), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun CustomThemeBuilderDialog(
    onSave: (name: String, primaryColor: Long, secondaryColor: Long, accentColor: Long, isDark: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalDaymarkColors.current
    var name by remember { mutableStateOf("My Theme") }

    val primarySwatches = listOf(0xFFFBFBF9, 0xFFF5F2EA, 0xFF0F1115, 0xFF1E222A, 0xFFFAF5F5)
    val secondarySwatches = listOf(0xFFF0EFEB, 0xFFE8E2D5, 0xFF1E232B, 0xFF2C3340, 0xFFEFE3E3)
    val accentSwatches = listOf(0xFF91A68A, 0xFFA76D38, 0xFF38BDF8, 0xFF60A5FA, 0xFFBE4B5F, 0xFF10B981)

    var selectedPrimary by remember { mutableStateOf(primarySwatches[0]) }
    var selectedSecondary by remember { mutableStateOf(secondarySwatches[0]) }
    var selectedAccent by remember { mutableStateOf(accentSwatches[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.primaryBackground,
        title = { Text("Custom Theme Creator", color = colors.textPrimary) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Theme Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 60% Primary
                Text("60% Background Surface", style = MaterialTheme.typography.labelSmall.copy(color = colors.textSecondary))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    primarySwatches.forEach { c ->
                        ColorSelectCircle(color = c, isSelected = selectedPrimary == c, onClick = { selectedPrimary = c })
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 30% Secondary
                Text("30% Container / Card", style = MaterialTheme.typography.labelSmall.copy(color = colors.textSecondary))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    secondarySwatches.forEach { c ->
                        ColorSelectCircle(color = c, isSelected = selectedSecondary == c, onClick = { selectedSecondary = c })
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 10% Accent
                Text("10% Accent / Focus", style = MaterialTheme.typography.labelSmall.copy(color = colors.textSecondary))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    accentSwatches.forEach { c ->
                        ColorSelectCircle(color = c, isSelected = selectedAccent == c, onClick = { selectedAccent = c })
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Preview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(selectedPrimary))
                        .padding(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(selectedSecondary))
                            .padding(12.dp)
                    ) {
                        Text("Theme Preview", color = Color(selectedAccent), fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val isDark = selectedPrimary == 0xFF0F1115 || selectedPrimary == 0xFF1E222A
                    onSave(name.trim(), selectedPrimary, selectedSecondary, selectedAccent, isDark)
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
            ) {
                Text("Save Theme", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.textSecondary)
            }
        }
    )
}

@Composable
fun ColorSelectCircle(color: Long, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(color))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}
