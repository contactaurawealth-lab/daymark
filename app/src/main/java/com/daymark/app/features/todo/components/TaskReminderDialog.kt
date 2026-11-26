package com.daymark.app.features.todo.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daymark.app.core.designsystem.LocalDaymarkColors
import com.daymark.app.domain.models.Task

@Composable
fun TaskReminderDialog(
    task: Task,
    onSetReminder: (triggerAtMillis: Long) -> Unit,
    onRemoveReminder: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalDaymarkColors.current

    val presetDelays = listOf(
        "15 minutes" to (15 * 60 * 1000L),
        "1 hour" to (60 * 60 * 1000L),
        "3 hours" to (3 * 60 * 60 * 1000L),
        "Tomorrow morning (9 AM)" to (12 * 60 * 60 * 1000L)
    )

    var selectedDelay by remember { mutableStateOf(presetDelays[0].second) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.primaryBackground,
        title = {
            Text(
                text = "Set Reminder",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Notify me for \"${task.title}\" in:",
                    style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary)
                )

                Spacer(modifier = Modifier.height(14.dp))

                presetDelays.forEach { (label, delayMillis) ->
                    val isSelected = selectedDelay == delayMillis
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) colors.accent else colors.secondaryContainer)
                            .clickable { selectedDelay = delayMillis }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color.White else colors.textPrimary
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSetReminder(System.currentTimeMillis() + selectedDelay)
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
            ) {
                Text("Set", color = Color.White)
            }
        },
        dismissButton = {
            Row {
                if (task.reminderTime != null) {
                    TextButton(onClick = onRemoveReminder) {
                        Text("Remove", color = Color(0xFFE53935))
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        }
    )
}
