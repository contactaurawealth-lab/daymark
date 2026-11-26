package com.daymark.app.features.todo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daymark.app.core.designsystem.LocalDaymarkColors
import com.daymark.app.domain.models.Task

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskTimerPickerSheet(
    task: Task? = null,
    defaultMinutes: Int = 25,
    onStartTimer: (durationMinutes: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalDaymarkColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val presets = listOf(5, 10, 15, 25, 45, 60)
    var selectedMinutes by remember { mutableStateOf(defaultMinutes) }
    var customMinutesText by remember { mutableStateOf("") }
    var isCustomSelected by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.primaryBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (task != null) "Focus Timer" else "Focus Mode",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                    )
                    Text(
                        text = task?.title ?: "Distraction-free focus session",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = colors.textSecondary
                        ),
                        maxLines = 1
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close", tint = colors.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Preset Durations",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = colors.textSecondary,
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { minutes ->
                    val isSelected = !isCustomSelected && selectedMinutes == minutes
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) colors.accent else colors.secondaryContainer)
                            .clickable {
                                isCustomSelected = false
                                selectedMinutes = minutes
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "$minutes min",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color.White else colors.textPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Duration Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customMinutesText,
                    onValueChange = {
                        customMinutesText = it.filter { ch -> ch.isDigit() }
                        if (customMinutesText.isNotEmpty()) {
                            isCustomSelected = true
                            selectedMinutes = customMinutesText.toIntOrNull() ?: 25
                        }
                    },
                    label = { Text("Custom minutes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val finalMinutes = if (isCustomSelected) {
                        customMinutesText.toIntOrNull()?.coerceAtLeast(1) ?: 25
                    } else {
                        selectedMinutes
                    }
                    onStartTimer(finalMinutes)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
            ) {
                Icon(Icons.Outlined.PlayArrow, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start $selectedMinutes min Timer", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
